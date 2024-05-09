import React from 'react'
import { Enrollee } from '@juniper/ui-core'

/**
 * Alert component which shows the number of remaining tasks for a participant.
 */
export default function RemainingTasksAlert({ enrollee }: { enrollee: Enrollee }) {
  const numTasks = enrollee.participantTasks.filter(participantTask => participantTask.status != 'COMPLETE').length

  return (<span>
    <span
      className={`rounded-circle alert-circle
    ${numTasks == 0 ? 'bg-secondary-subtle' : 'bg-primary text-white'}`}>
      {numTasks}
    </span>
  </span>)
}
