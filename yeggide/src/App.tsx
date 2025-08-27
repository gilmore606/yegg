import React from 'react'
import CodeEditor from "./components/CodeEditor";

class App extends React.Component<any, any> {

  render() {
    return (
        <div className="ideContainer">
          <div className="projectPane">
            PROJECT
          </div>
          <div className="editorPane">
            EDIT
            <CodeEditor />
          </div>
        </div>
    )
  }
}

export default App
