import classNames from 'classnames'
import React from 'react'
import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

type CollapsibleSectionProps = {
    targetSelector: string;
    sectionLabel: string;
};

/**
 * Button that toggles a collapsible section
 */
export const CollapsibleSectionButton = (props: CollapsibleSectionProps) => {
  const { targetSelector, sectionLabel } = props
  return (
    <div className="pb-1">
      <button
        aria-controls={targetSelector}
        aria-expanded="false"
        className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
        data-bs-target={targetSelector}
        data-bs-toggle="collapse"
      >
        <span className={'form-label fw-semibold mb-0'}>{sectionLabel}</span>
        <span className="text-center px-2">
          <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
          <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
        </span>
      </button>
    </div>
  )
}
