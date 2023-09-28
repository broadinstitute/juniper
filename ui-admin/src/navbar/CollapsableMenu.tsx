import React, { useId } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import classNames from 'classnames'

/** collapsable thing -- uses bootstrap collapse styling */
export default function CollapsableMenu({ header, content, headerClass='text-white fw-bold' }:
{header: React.ReactNode, content: React.ReactNode, headerClass?: string}) {
  const contentId = useId()
  const targetSelector  = `#${contentId}`
  return <div>
    <div className="pb-1">
      <button
        aria-controls={targetSelector}
        aria-expanded="true"
        className={classNames('btn-link btn w-100 py-2 px-0 d-flex text-decoration-none', headerClass)}
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
