import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleCheck, faCircleDot, faCircleExclamation, faCircleQuestion } from '@fortawesome/free-solid-svg-icons'
import React from 'react'

export type KitReturnType = 'IN_PERSON' | 'RETURN_LABEL'
export type StepStatus = 'COMPLETE' | 'INCOMPLETE' | 'ERROR'

export const stepStatusToIcon = (status: StepStatus) => {
  switch (status) {
    case 'COMPLETE':
      return <FontAwesomeIcon className="text-success me-1" icon={faCircleCheck}/>
    case 'INCOMPLETE':
      return <FontAwesomeIcon className="text-muted me-1" icon={faCircleDot}/>
    case 'ERROR':
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleExclamation}/>
    default:
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleQuestion}/>
  }
}
