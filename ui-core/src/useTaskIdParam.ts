import { useGlobalSearchParams } from './util/GlobalSearchParamsProvider'

const TASK_ID_PARAM = 'taskId'
/** gets the task ID from the URL */
export const useTaskIdParam = (): {taskId: string | null, setTaskId: (taskId: string) => void} => {
  const { searchParams, setSearchParams } = useGlobalSearchParams()
  return {
    taskId: searchParams.get(TASK_ID_PARAM),
    setTaskId: (taskId: string) => {
      searchParams.set(TASK_ID_PARAM, taskId)
      setSearchParams(searchParams)
    }
  }
}
