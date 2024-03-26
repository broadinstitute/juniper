import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'

/**
 *
 */
const AddParticipant = () => {
  const navigate = useNavigate()
  const { studyShortcode } = useParams()
  const goToStudyEnrollRouter = () => {
    // Assuming you have access to the studyShortcode

    navigate(`/studies/${studyShortcode}/join/preEnroll`)
    // navigate('..//preEnroll', { replace: true })
  }

  return (
    <div>
      <button onClick={goToStudyEnrollRouter}>Go to Study Enroll</button>
    </div>
  )
}

export default AddParticipant

