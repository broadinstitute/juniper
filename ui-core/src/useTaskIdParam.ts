import { useSearchParams } from 'react-router-dom'

const TASK_ID_PARAM = 'taskId'
/** gets the task ID from the URL */
export const useTaskIdParam = (): string | null => {
  const [searchParams] = useSearchParams()
  return searchParams.get(TASK_ID_PARAM)
}
