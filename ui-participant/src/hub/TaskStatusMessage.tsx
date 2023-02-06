import React from 'react'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheck} from "@fortawesome/free-solid-svg-icons/faCheck";
import {faTimes} from "@fortawesome/free-solid-svg-icons/faTimes";
import {faInfo} from "@fortawesome/free-solid-svg-icons/faInfo";

/** I am 100% sure UX will come up with better icons/images than this -- but these are just for now */
const ICON_MAP: Record<string, React.ReactNode> = {
  success: <FontAwesomeIcon icon={faCheck}/>,
  failure: <FontAwesomeIcon icon={faTimes}/>,
  info: <FontAwesomeIcon icon={faInfo}/>
}
/**
 * we may want to upgrade this to a toast library with snazz animations, but for now we'll keep the bundle small
 * and have this be very simple
 */
export default function TaskStatusMessage({
                                            content,
                                            messageType
                                          }: { content: React.ReactNode, messageType: string }) {
  return <div className="text-center p-2" style={{background: '#eee'}}>
    <span>{content}</span><span className="ms-3">{ICON_MAP[messageType]}</span>
  </div>
}
