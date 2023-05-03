import { calculateIdleData } from './IdleStatusMonitor'

describe('IdleStatusMonitor calculateIdleData', () => {
  it('chooses to render nothing for non-idle session', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: undefined,
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

  it('chooses to render an idle warning', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 6001,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(false)
    expect(idleData.showCountdown).toBe(true)
    expect(idleData.secondsUntilTimedOut).toBe(3)
    expect(idleData.millisecondsUntilNextUpdate).toBe(999)
  })

  it('expires an idle session', () => {
    const idleData = calculateIdleData({
      lastRecordedActivity: 0,
      currentTime: 10001,
      maxIdleSessionDuration: 10000,
      idleWarningDuration: 4000
    })

    expect(idleData.timedOut).toBe(true)
    expect(idleData.showCountdown).toBe(true)
    expect(idleData.secondsUntilTimedOut).toBe(0)
    expect(idleData.millisecondsUntilNextUpdate).toBe(250)
  })
})
