import { calculateIdleData } from './IdleStatusMonitor'

describe('IdleStatusMonitor calculateIdleData', () => {
  it('chooses to render nothing for non-idle session', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 0,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(false)
    expect(idleData.showCountdown).toBe(false)
    expect(idleData.secondsUntilTimedOut).toBe(10)
    expect(idleData.millisecondsUntilNextUpdate).toBe(6000)
  })

  it('calculates correct delay for somewhat idle session', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 1000,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(false)
    expect(idleData.showCountdown).toBe(false)
    expect(idleData.secondsUntilTimedOut).toBe(9)
    expect(idleData.millisecondsUntilNextUpdate).toBe(5000)
  })

  it('considers a session active until the last millisecond', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 5999,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(false)
    expect(idleData.showCountdown).toBe(false)
    expect(idleData.secondsUntilTimedOut).toBe(5)
    // Would be 1, but never less than 250
    expect(idleData.millisecondsUntilNextUpdate).toBe(250)
  })

  it('chooses to render an idle warning', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 6000,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(false)
    expect(idleData.showCountdown).toBe(true)
    expect(idleData.secondsUntilTimedOut).toBe(4)
    expect(idleData.millisecondsUntilNextUpdate).toBe(1000)
  })

  it('displays one-based seconds remaining', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 7500,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(false)
    expect(idleData.showCountdown).toBe(true)
    expect(idleData.secondsUntilTimedOut).toBe(3)
    expect(idleData.millisecondsUntilNextUpdate).toBe(500)
  })

  it('expires an idle session', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 10000,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(true)
    expect(idleData.showCountdown).toBe(true)
    expect(idleData.secondsUntilTimedOut).toBe(0)
    expect(idleData.millisecondsUntilNextUpdate).toBeGreaterThanOrEqual(250)
  })

  it('avoids negative secondsUntilTimedOut', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 12000,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(true)
    expect(idleData.showCountdown).toBe(true)
    expect(idleData.secondsUntilTimedOut).toBe(0)
    expect(idleData.millisecondsUntilNextUpdate).toBeGreaterThanOrEqual(250)
  })
})
