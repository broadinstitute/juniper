import { Enrollee } from '../api/api'
import React from 'react'

/**
 *
 */
export default function AlertComponent({ enrollee } :{enrollee:Enrollee}) {
  function getNumberOfTasks(enrollee: Enrollee) {
    let waitingTasks = 0
    enrollee.participantTasks.forEach(participantTask => {
      if (participantTask.status != 'COMPLETE') {
        waitingTasks++
      }
    })
    return waitingTasks
  }
  return (<span>
    <span
      className={`rounded-circle alert-circle
    ${getNumberOfTasks(enrollee) == 0 ? 'bg-secondary-subtle' : 'bg-danger text-white'}`}>
      {getNumberOfTasks(enrollee)}
    </span>
  </span>)
}
