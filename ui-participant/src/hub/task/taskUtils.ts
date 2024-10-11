import { Enrollee, HubResponse, ParticipantTask } from '@juniper/ui-core'

/** returns the next actionable task for the enrollee, or undefined if there is no remaining task */
export function getNextTask(enrollee: Enrollee, sortedTasks: ParticipantTask[]) {
  const nextTask = sortedTasks.find(task => isTaskAccessible(task, enrollee) && isTaskActive(task))
  return nextTask
}

export const TASK_TYPE_ORDER = ['CONSENT', 'SURVEY']
export const TASK_STATUS_ORDER = ['IN_PROGRESS', 'NEW', 'COMPLETE']

/** Sorts tasks based on their types, then based on status, and then based on their internal ordering */
export function taskComparator(taskA: ParticipantTask, taskB: ParticipantTask) {
  const typeOrder = TASK_TYPE_ORDER.indexOf(taskA.taskType) - TASK_TYPE_ORDER.indexOf(taskB.taskType)
  if (typeOrder != 0) {
    return typeOrder
  }
  const statusOrder = TASK_STATUS_ORDER.indexOf(taskA.status) - TASK_STATUS_ORDER.indexOf(taskB.status)
  if (statusOrder != 0) {
    return statusOrder
  }
  return taskA.taskOrder - taskB.taskOrder
}

/** returns a string for including in a <Link to={}> link to be navigated by the participant */
export function getTaskPath(task: ParticipantTask, enrolleeShortcode: string,
  studyShortcode: string, isPrint = false): string {
  const url = `study/${studyShortcode}/enrollee/${enrolleeShortcode}/${task.taskType.toLowerCase()}`
        +  `/${task.targetStableId}/${task.targetAssignedVersion}${isPrint ? '/print' : ''}?taskId=${task.id}`
  return url
}

/** is the task actionable by the user? the rules are:
 * consent forms are always actionable.
 * nothing else is actionable until consent
 * required tasks must be done in-order
 * non-required tasks are not actionable until all required tasks are cleared */
export function isTaskAccessible(task: ParticipantTask, enrollee: Enrollee) {
  if (task.taskType === 'CONSENT' || task.status === 'COMPLETE') {
    return true
  }
  const openConsents = enrollee.participantTasks
    .filter(task => task.taskType === 'CONSENT' && task.status !== 'COMPLETE')
  if (openConsents.length) {
    return false
  }
  const openRequiredTasks = enrollee.participantTasks.filter(task => task.blocksHub && task.status !== 'COMPLETE')
    .sort((a, b) => a.taskOrder - b.taskOrder)
  if (openRequiredTasks.length) {
    return task.id === openRequiredTasks[0].id
  }
  return true
}

/** is the task ready to be worked on (not done or rejected) */
export function isTaskActive(task: ParticipantTask) {
  return ['NEW', 'VIEWED', 'IN_PROGRESS'].includes(task.status)
}

export function getSortedActiveTasks(tasks: ParticipantTask[], taskType: string): ParticipantTask[] {
  return tasks
    .filter(task => task.taskType === taskType && isTaskActive(task))
    .sort(taskComparator)
}

export function getNextConsentTask(hubResponse: HubResponse) {
  const sortedActiveConsentTasks = getSortedActiveTasks(hubResponse.enrollee.participantTasks, 'CONSENT')
  return getNextTask(hubResponse.enrollee, sortedActiveConsentTasks)
}
