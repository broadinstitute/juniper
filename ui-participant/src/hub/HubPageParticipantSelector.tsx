import React from 'react'
import { useUser } from '../providers/UserProvider'
import HubPageParticipantSelectorItem from './HubPageParticipantSelectorItem'

/**
 * Selector for changing participant on the hub page. Works by changing the active user in local storage.
 */
export default function HubPageParticipantSelector() {
  const {
    enrollees,
    relations
  } = useUser()

  function getTotalTasks() {
    const pendingEnrolleeTasks = enrollees.flatMap(enrollee =>
      enrollee.participantTasks.filter(participantTask =>
        participantTask.status != 'COMPLETE'
      )
    )
    return pendingEnrolleeTasks.length
  }


  return (
    <div className="dropdown hub-dashboard-background flex-grow-1">
      <button
        className="btn btn-outline-primary dropdown-toggle
                  w-100 position-relative d-flex justify-content-center align-items-center my-2"
        type="button"
        data-bs-toggle="dropdown" aria-expanded="false" id="dropdownMenuButton">
        Participants and Tasks
        <span className={` alert-circle position-absolute rounded-circle
                    ${getTotalTasks() == 0 ? 'bg-secondary-subtle' : 'bg-danger text-white'}`}
        style={{ right: '12px', top: '50%', transform: 'translateY(-50%)' }}>
          {getTotalTasks()}
        </span>
      </button>

      <ul className="dropdown-menu w-100" aria-labelledby="dropdownMenuButton">

        <HubPageParticipantSelectorItem
          enrollee={enrollees[0]}
          relationshipType={undefined}/>

        {relations?.map((enrolleeRelation, idx) => (
          <HubPageParticipantSelectorItem
            key={idx}
            enrollee={enrolleeRelation.targetEnrollee}
            relationshipType={enrolleeRelation.relationshipType}/>
        ))}
      </ul>
    </div>
  )
}
