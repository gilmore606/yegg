import React from 'react'
import CodeEditor from "./components/CodeEditor";

class App extends React.Component<any, any> {

  render() {
    return (
        <div class="ideContainer">
          <div class="projectPane">
            PROJECT
          </div>
          <div class="editorPane">
            <CodeEditor />
          </div>
        </div>
    )
  }
}

export default App
