function Get-Auth {
    $user = Read-Host -Prompt "username"
    $pass = Read-Host -Prompt "password"
    $body = @{ username=$user; password=$pass } | ConvertTo-Json
    return Invoke-RestMethod -Method post -Uri "http://localhost:8080/auth" -Body $body -ContentType "application/json"
}

$token = Get-Auth
$user = "?"

$quit = 0
while ($quit -eq 0) {
    $prompt = "($user)"
    $input = Read-Host -Prompt $prompt
    $words = $input.Split()
    switch ($words[0]) {
        "" {}
        {($_ -eq "h") -or ($_ -eq "?")} {
            Write-Host "Edit verb:  e trait.verbName"
            Write-Host "Re-auth:    auth"
            Write-Host "Help:       ?"
            Write-Host "Quit:       q"
        }
        "auth" {
            $token = Get-Auth
        }
        "e" {
            $uri = $words[1].replace(".", "/").replace("$", "")
            $headers = @{ "Authorization" = "Bearer $token"}
            $tempfile = [System.IO.Path]::GetTempFileName()
            $filename = [System.IO.Path]::ChangeExtension($tempfile, ".yegg")
            Invoke-RestMethod -Method get -Uri "http://localhost:8080/verb/$uri" -Headers $headers -OutFile $filename
            start $filename

            $this_time = (get-item $filename).LastWriteTime
            $last_time = $this_time
            $done = 0
            while ($done -eq 0) {
                if ($last_time -ne $this_time) {
                    $last_time = $this_time
                    $newcode = Get-Content -Path $filename -Raw
                    Write-Host "Result:"
                    Invoke-RestMethod -Method put -Uri "http://localhost:8080/verb/$uri" -Headers $headers -Body $newcode
                    $done = 1
                }
                sleep 1
                $this_time = (Get-Item $filename).LastWriteTime
            }
        }
        "q" {
            $quit = 1
        }
        default {
            Write-Host "I don't understand that.  (? for help)"
        }
    }
}
