import React from 'react'
import { useUser } from '../providers/UserProvider'
import HubPageParticipantSelectorItem from './HubPageParticipantSelectorItem'

/**
 * Item in a dropdown list which allows the user to switch between different participants.
 */
export default function HubPageParticipantSelector() {
  const {
    enrollees,
    relations,
    profile
  } = useUser()

  function getTotalTasks() {
    const pendingEnrolleeTasks = enrollees.flatMap(enrollee =>
      enrollee.participantTasks.filter(participantTask =>
        participantTask.status != 'COMPLETE'
      )
    )
    const pendingRelationTasks = relations?.flatMap(enrolleeRelation =>
      enrolleeRelation.targetEnrollee.participantTasks.filter(participantTask =>
        participantTask.status != 'COMPLETE'
      )
    )
    return pendingEnrolleeTasks.length + pendingRelationTasks?.length
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

        <HubPageParticipantSelectorItem enrollee={enrollees[0]} profile={profile}
          relationshipType={undefined}/>

        {relations?.map((enrolleeRelation, idx) => (
          <HubPageParticipantSelectorItem
            key={idx}
            enrollee={enrolleeRelation.targetEnrollee}
            profile={enrolleeRelation.targetEnrollee.profile}
            relationshipType={enrolleeRelation.relationshipType}/>
        ))}
      </ul>
    </div>
  )
}
