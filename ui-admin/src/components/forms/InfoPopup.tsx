import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import React, { useId } from 'react'
import { OverlayTrigger, Popover } from 'react-bootstrap'
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons'
import { Placement } from 'react-bootstrap/types'
import { OverlayTriggerType } from 'react-bootstrap/OverlayTrigger'


/** this acts as a button, but replaces the button content prop that can be any react node */
export type InfoPopupProps = Omit<JSX.IntrinsicElements['button'], 'content'> & {
  content: React.ReactNode // content of the popup
  target?: React.ReactNode  // the element to attach the popover to
  placement?: Placement
  trigger?: OverlayTriggerType[] // what actions trigger the popup, for hover tips, use ['hover', 'focus']
  className?: string
}

/**
 * thin wrapper around OverlayTrigger and Popover to render popup help content
 * popup will work even when underlying element is disabled.
 * All arguments are passed through to either Popover or OverlayTrigger
 */
export default function InfoPopup({
  content, // the content of the popover
  target=<FontAwesomeIcon icon={faInfoCircle}/>,
  trigger=['click'],
  placement='top',
  className='tooltip-wide'
}: InfoPopupProps) {
  const id = useId()
  const popoverContent = <Popover id={id} className={className}>
    <div className="p-2">{content}</div>
  </Popover>
  return <OverlayTrigger rootClose trigger={trigger} placement={placement} overlay={popoverContent}>
    <button aria-label="info popup" className="btn btn-secondary p-0 mx-2">{target}</button>
  </OverlayTrigger>
}
