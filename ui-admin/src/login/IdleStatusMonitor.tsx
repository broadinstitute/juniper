/*
 * ATTENTION! This is a copy of IdleStatusMonitor from ui-participant. Soon (hopefully), we will have a way to share
 * code between these two modules. Until then, these MUST be kept in sync with each other.
 *
 * Note: UserProvider has a different path
 */
import React, { useEffect, useRef, useState } from 'react'
import { Modal } from 'react-bootstrap'
import _ from 'lodash'
import { useUser } from 'user/UserProvider'

/*
 * This code was copied from Terra UI and modified to:
 *   - use JSX instead of react-hyperscript-helpers
 *   - use Bootstrap Modal instead of react-modal for the idle warning
 *   - get user and logoutUser callbacks from useUser() instead of using Terra UI's authStore and a custom doSignOut
 *   - remove the unnecessary forced Google logout
 *   - a bit of renaming to hopefully improve readability
 *
 * Also added some tests for the idle time calculations.
 */

/**
 * @property timedOut whether the user's session should be closed due to inactivity
 * @property showCountdown whether an idle warning should be displayed to the user
 * @property secondsUntilTimedOut time remaining until user's session should be closed, in seconds for user display
 * @property millisecondsUntilNextUpdate time before one of these values is likely to change (for setTimeout())
 */
type IdleData = {
  timedOut: boolean,
  showCountdown: boolean,
  secondsUntilTimedOut: number,
  millisecondsUntilNextUpdate: number
}

/**
 * Performs the math needed to determine any idle time information that needs to be acted on or surfaced to the
 * user. At a high level, the possible states are:
 *   - Active session, no idle warning
 *   - Session has been idle and will time out soon; show the user a warning, including the number of seconds remaining
 *   - Session has timed out and the user needs to be signed out
 *
 * In addition, we can calculate how long we should wait before we need to perform these calculations again.
 *
 * @param currentTime the current time, in milliseconds since epoch (see useCurrentTime())
 * @param lastRecordedActivity the time of last user activity, in milliseconds since epoch
 * @param timeout duration of inactivity before an idle session should be closed, in milliseconds
 * @param idleWarningDuration duration during which to warn an idle user before closing their session, in milliseconds
 */
export const calculateIdleData = ({ currentTime, lastRecordedActivity, maxIdleSessionDuration, idleWarningDuration }: {
  currentTime: number,
  lastRecordedActivity: number,
  maxIdleSessionDuration: number,
  idleWarningDuration: number
}): IdleData => {
  const timeoutTime = lastRecordedActivity + maxIdleSessionDuration

  const timedOut = currentTime >= timeoutTime
  const showCountdown = currentTime >= (timeoutTime - idleWarningDuration)
  const millisecondsUntilTimedOut = timeoutTime - currentTime
  const secondsUntilTimedOut = Math.floor((millisecondsUntilTimedOut - 1) / 1000)
  const millisecondsUntilNextUpdate = showCountdown
    ? Math.max(250, millisecondsUntilTimedOut - secondsUntilTimedOut * 1000)
    : Math.max(250, millisecondsUntilTimedOut - idleWarningDuration)

  // Now that we've done all the calculations, return a one-based secondsUntilTimedOut to make it more appropriate for
  // display to the user
  const displaySecondsUntilTimeout = Math.max(0, secondsUntilTimedOut + 1)
  return { timedOut, showCountdown, secondsUntilTimedOut: displaySecondsUntilTimeout, millisecondsUntilNextUpdate }
}

export const IdleStatusMonitor = ({ maxIdleSessionDuration, idleWarningDuration }: {
  maxIdleSessionDuration: number,
  idleWarningDuration: number
}) => {
  const { user, logoutUser } = useUser()

  return user
    ? <InactivityTimer
      maxIdleSessionDuration={maxIdleSessionDuration}
      idleWarningDuration={idleWarningDuration}
      doSignOut={logoutUser}/>
    : null
}

const IdleWarningModal = ({ secondsUntilTimedOut, onDismiss }: {
  secondsUntilTimedOut: number,
  onDismiss: () => void
}) => {
  const minutes = Math.floor(secondsUntilTimedOut / 60)
  const seconds = secondsUntilTimedOut % 60
  const timeRemaining = `${minutes}:${seconds.toString().padStart(2, '0')}`
  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header>
      <Modal.Title>Your session is about to expire</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>To maintain security and protect your data, you will be logged out in {timeRemaining}.</p>
    </Modal.Body>
  </Modal>
}

const InactivityTimer = ({ maxIdleSessionDuration, idleWarningDuration, doSignOut }: {
  maxIdleSessionDuration: number,
  idleWarningDuration: number,
  doSignOut: () => void
}) => {
  const [idleModalVisible, setIdleModalVisible] = useState(false)
  const [signOutTriggered, setSignOutTriggered] = useState(false)
  const [currentTime, setNextUpdateDelay] = useCurrentTime()
  const lastRecordedActivity = useRef<number>(Date.now())

  const { timedOut, showCountdown, secondsUntilTimedOut, millisecondsUntilNextUpdate } = calculateIdleData({
    currentTime, lastRecordedActivity: lastRecordedActivity.current, maxIdleSessionDuration, idleWarningDuration
  })

  useEffect(() => {
    const targetEvents = ['click', 'keydown']
    const updateLastActive = () => { lastRecordedActivity.current = Date.now() }

    if (!lastRecordedActivity.current) {
      updateLastActive()
    }

    _.forEach(targetEvents, event => document.addEventListener(event, updateLastActive, true))

    return () => {
      _.forEach(targetEvents, event => document.removeEventListener(event, updateLastActive, true))
    }
  }, [])

  useEffect(() => {
    if (timedOut && !signOutTriggered) {
      doSignOut()
      setSignOutTriggered(true)
    }
  }, [doSignOut, signOutTriggered, timedOut])

  if (idleModalVisible != showCountdown) {
    setIdleModalVisible(showCountdown)
  }

  setNextUpdateDelay(millisecondsUntilNextUpdate)

  return idleModalVisible
    ? <IdleWarningModal secondsUntilTimedOut={secondsUntilTimedOut} onDismiss={() => setIdleModalVisible(false)}/>
    : null
}

// Copied from Terra UI
// Modified to yield execution before looping and starting the next delay
export const useCurrentTime = (initialDelay = 250) => {
  const [currentTime, setCurrentTime] = useState(Date.now())
  const signal = useCancellation()
  const delayRef = useRef(initialDelay)
  useEffect(() => {
    const poll = async () => {
      while (!signal.aborted) {
        await delay(delayRef.current)
        setCurrentTime(Date.now())
        // yield to give the containing component an opportunity to set a new delay time
        await delay(0)
      }
    }
    poll()
  }, [])
  return [currentTime, (delay: number) => { delayRef.current = delay }] as const
}

// Copied from Terra UI without modification for use by useCurrentTime
export const useCancellation = (): AbortSignal => {
  const controller = useRef<AbortController>()
  useEffect(() => {
    const instance = controller.current
    return () => instance?.abort()
  }, [])
  if (!controller.current) {
    controller.current = new window.AbortController()
  }
  return controller.current.signal
}

// Copied from Terra UI without modification for use by useCurrentTime
export const delay = (ms: number) => {
  return new Promise(resolve => setTimeout(resolve, ms))
}
