import React from 'react'
import { Route, Routes } from 'react-router-dom'
import ExportHelp from './ExportHelp'
import HelpPage from './HelpPage'

/** routes across individual help pages -- catches any unmatched routes to the main index */
export default function HelpRouter() {
  return <div className="container p-4">
    <Routes>
      <Route path="export" element={<ExportHelp/>}/>
      <Route index element={<HelpPage/>}/>
      <Route path="*" element={<HelpPage/>}/>
    </Routes>
  </div>
}
