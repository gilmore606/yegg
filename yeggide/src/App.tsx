import React from 'react'
import CodeEditor from "./components/CodeEditor";
import SplitPane from "react-split-pane";

class App extends React.Component<any, any> {

  render() {
    return (
        <SplitPane split="vertical" className="ideContainer" defaultSize={150} minSize={50} maxSize={400} allowResize={true}>
            <div className="projectPane pane">
                PROJECT
            </div>
            <SplitPane split="horizontal" defaultSize={50} allowResize={false}>
                <div className="tabsPane pane">
                    tabs
                </div>
                <div className="editorPane pane">
                    <CodeEditor />
                </div>
            </SplitPane>
        </SplitPane>
    )
  }
}

export default App
