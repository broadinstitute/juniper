
const TASK_ID_PARAM = 'taskId'
/** gets the task ID from the URL */
export const useTaskIdParam = (searchParams: URLSearchParams, setSearchParams: (params: URLSearchParams) => void): {
  taskId: string | null,
  setTaskId: (taskId: string) => void
} => {
  return {
    taskId: searchParams.get(TASK_ID_PARAM),
    setTaskId: (taskId: string) => {
      searchParams.set(TASK_ID_PARAM, taskId)
      setSearchParams(searchParams)
    }
  }
}
