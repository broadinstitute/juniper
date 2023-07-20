import React from 'react'
import { Link } from 'react-router-dom'

/** shows the root help page.  No structure yet */
export default function HelpPage() {
  return <div>
    <h1 className="h3 mb-3">Juniper help topics</h1>
    <div>
      <Link to="export">Participant export</Link>
    </div>
  </div>
}
