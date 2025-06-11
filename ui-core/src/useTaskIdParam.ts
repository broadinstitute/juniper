import { useSearchParams } from 'react-router-dom'

const TASK_ID_PARAM = 'taskId'
/** gets the task ID from the URL */
export const useTaskIdParam = (): {taskId: string | null, setTaskId: (taskId: string) => void} => {
  const [searchParams, setSearchParams] = useSearchParams()
  return {
    taskId: searchParams.get(TASK_ID_PARAM),
    setTaskId: (taskId: string) => {
      searchParams.set(TASK_ID_PARAM, taskId)
      setSearchParams(searchParams)
    }
  }
}
