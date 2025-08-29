import React from 'react'
import CodeEditor from "./components/CodeEditor";
import "allotment/dist/style.css"
import './index.css'
import './App.css'
import {Allotment} from "allotment";

class App extends React.Component<any, any> {

  render() {
    return (
        <div style={{ width: "100vw", height: "100vh" }}>
            <Allotment>
                <Allotment.Pane preferredSize={150} maxSize={400} minSize={50}>
                    <div className="projectPane pane">
                        PROJECT
                    </div>
                </Allotment.Pane>
                <Allotment.Pane>
                    <Allotment vertical>
                        <Allotment.Pane preferredSize={60} maxSize={60} minSize={60}>
                            <div className="tabsPane pane">
                                tabs
                            </div>
                        </Allotment.Pane>
                        <Allotment.Pane>
                            <div className="editorPane pane">
                                <CodeEditor />
                            </div>
                        </Allotment.Pane>
                    </Allotment>
                </Allotment.Pane>
            </Allotment>
        </div>
    )
  }
}

export default App
