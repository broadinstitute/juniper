import { Trigger } from '@juniper/ui-core'

export const triggerName = (trigger: Trigger) => {
  if (trigger.actionType === 'NOTIFICATION' || trigger.actionType === 'ADMIN_NOTIFICATION') {
    return trigger.emailTemplate.name
  } else if (trigger.actionType === 'TASK_STATUS_CHANGE') {
    const targets = trigger.actionTargetStableIds.length ? trigger.actionTargetStableIds.join(',') : '[task]'
    return `Mark ${targets} as ${trigger.statusToUpdateTo ?? '[status]'}`
  } else if (trigger.triggerType === 'TASK_REMINDER') {
    return `Remind after ${minutesToDayString(trigger.afterMinutesIncomplete)}`
  }
}

export const minutesToDayString = (minutes: number) => {
  return `${Math.round(minutes * 10 / (60 * 24)) / 10  } days`
}
