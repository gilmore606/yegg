import React from 'react'
import MonacoEditor, { type MonacoEditorProps } from 'react-monaco-editor'

interface CodeEditorState {
    code: string
}

class CodeEditor extends React.Component<{}, CodeEditorState> {
    private editorRef: React.RefObject<any>

    constructor(props: {}) {
        super(props)
        this.state = {
            code: '// type your code.........'
        }
        this.editorRef = React.createRef()
    }

    editorWillMount = (monaco: any) => {
        console.log('Editor will mount:', monaco)
    }

    editorDidMount = (editor: any, monaco: any) => {
        this.editorRef.current = editor
        console.log('Editor mounted:', editor, monaco)
    }

    onChange = (newValue: string) => {
        this.setState({ code: newValue })
    }

    render() {
        const options: MonacoEditorProps['options'] = { automaticLayout: true }

        return (
                <MonacoEditor
                    language="javascript"
                    theme="vs-dark"
                    value={this.state.code}
                    options={options}
                    editorWillMount={this.editorWillMount}
                    editorDidMount={this.editorDidMount}
                    onChange={this.onChange}
                />
        )
    }
}

export default CodeEditor
