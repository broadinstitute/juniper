import {
  Enrollee,
  HubResponse
} from '../types/user'
import { ParticipantTask } from '../types/task'

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

  // do not consider removed surveys in this logic
  const tasks = enrollee.participantTasks.filter(t => isTaskVisible(t))

  const openConsents = tasks
    .filter(task => task.taskType === 'CONSENT' && task.status !== 'COMPLETE')
  if (openConsents.length) {
    return false
  }

  const openRequiredTasks = tasks
    .filter(task => task.blocksHub)
    .sort((a, b) => a.taskOrder !== b.taskOrder
      ? a.taskOrder - b.taskOrder
      : (a.targetStableId || '').localeCompare(b.targetStableId || ''))
  if (openRequiredTasks.length === 0) {
    return true
  }

  // make sure the task before this task has at least one completed survey -
  // even if the previous survey has something new/in-progress/removed, if it has
  // a single completed response it should be considered done.
  let lastTaskStableId = ''
  let lastTaskIsComplete = true // first required task always accessible
  for (let i = 0; i < openRequiredTasks.length; i++) {
    if (task.targetStableId === openRequiredTasks[i].targetStableId) {
      return lastTaskIsComplete
    }

    if (openRequiredTasks[i].targetStableId === lastTaskStableId) {
      if (!lastTaskIsComplete) {
        lastTaskIsComplete = openRequiredTasks[i].status === 'COMPLETE'
      }
    } else {
      lastTaskStableId = openRequiredTasks[i].targetStableId || ''
      lastTaskIsComplete = openRequiredTasks[i].status === 'COMPLETE'
    }
  }

  return lastTaskIsComplete
}

/** is the task ready to be worked on (not done or rejected) */
export function isTaskActive(task: ParticipantTask) {
  return ['NEW', 'VIEWED', 'IN_PROGRESS'].includes(task.status)
}

export function isTaskVisible(task: ParticipantTask) {
  return ['NEW', 'VIEWED', 'IN_PROGRESS', 'COMPLETE'].includes(task.status)
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
