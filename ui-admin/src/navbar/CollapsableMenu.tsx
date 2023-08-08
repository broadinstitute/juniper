import React, { useId } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'

/** collapsable thing -- uses bootstrap collapse styling */
export default function CollapsableMenu({ header, content }: {header: React.ReactNode, content: React.ReactNode}) {
  const contentId = useId()
  const targetSelector  = `#${contentId}`
  return <div className="pt-3">
    <div className="py-1">
      <button
        aria-controls={targetSelector}
        aria-expanded="true"
        className="btn-link btn w-100 py-2 px-0 d-flex fw-bold text-decoration-none text-white"
        data-bs-target={targetSelector}
        data-bs-toggle="collapse"
      >
        <span className="text-center" style={{ width: 30 }}>
          <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
          <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
        </span>
        {header}
      </button>
    </div>
    <div className="collapse show" id={contentId} style={{ paddingLeft: '30px' }}>
      {content}
    </div>
  </div>
}
