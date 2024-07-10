import React, { useMemo, useState } from 'react'
import Api, { ParticipantTaskListDto, StudyEnvironmentSurvey } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout, renderEmptyMessage } from 'util/tableUtils'
import {
  Enrollee,
  instantToDateString,
  instantToDefaultString,
  ParticipantNote,
  ParticipantTask,
  StudyEnvParams
} from '@juniper/ui-core'
import { paramsFromContext, StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import _truncate from 'lodash/truncate'
import { studyEnvParticipantPath } from '../participants/ParticipantsRouter'
import { Link } from 'react-router-dom'
import { useLoadingEffect } from 'api/api-utils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faEdit } from '@fortawesome/free-solid-svg-icons'
import { useUser } from 'user/UserProvider'
import { IconButton } from '../../components/forms/Button'
import { AdminTaskEditModal } from './AdminTaskEditor'
import { renderPageHeader } from 'util/pageUtils'


/** show the lists of the user's tasks and all tasks */
export default function AdminTaskList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const [taskData, setTaskData] = useState<ParticipantTaskListDto>({
    tasks: [], enrollees: [], participantNotes: []
  })
  const [sorting, setSorting] = useState<SortingState>([
    { id: 'status', desc: true },
    { id: 'createdAt', desc: true }
  ])
  const { users } = useAdminUserContext()
  const [selectedTask, setSelectedTask] = useState<ParticipantTask>()
  const [showEditModal, setShowEditModal] = useState(false)


  const columns: ColumnDef<ParticipantTask>[] = [{
    header: 'Task',
    id: 'taskDescription',
    cell: info => taskDescription(info.row.original, paramsFromContext(studyEnvContext), studyEnvContext, taskData)
  }, {
    header: 'Task Type',
    accessorKey: 'taskType',
    cell: info => renderTaskTypeColumn(info.row.original)
  }, {
    header: 'Status',
    accessorKey: 'status',
    cell: info => renderStatusColumn(info.row.original)
  }, {
    header: 'Created',
    accessorKey: 'createdAt',
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Assignee',
    accessorKey: 'assignedAdminUserId',
    cell: info => users.find(user => user.id === info.getValue())?.username
  }, {
    header: '',
    id: 'actions',
    cell: info => <IconButton icon={faEdit} onClick={() => editTask(info.row.original)} aria-label="edit"/>
  }]

  const allTasksTable = useReactTable({
    data: taskData.tasks,
    columns,
    state: { sorting },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.fetchAdminTasksByStudyEnv(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      ['enrollee', 'participantNote'])

    setTaskData(result)
  }, [studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
    studyEnvContext.currentEnv.environmentName])

  const editTask = (task: ParticipantTask) => {
    setSelectedTask(task)
    setShowEditModal(true)
  }
  const onDoneEditing = (updatedTask?: ParticipantTask) => {
    setSelectedTask(undefined)
    setShowEditModal(false)
    if (updatedTask) {
      // for now, just reload the whole list if a task is updated
      reload()
    }
  }

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Tasks') }
    <LoadingSpinner isLoading={isLoading}>
      <MyTaskList studyEnvContext={studyEnvContext} taskData={taskData}/>
      <h3 className="h4 mt-5">All tasks</h3>
      { basicTableLayout(allTasksTable) }
      { renderEmptyMessage(taskData.tasks, 'No tasks') }
    </LoadingSpinner>
    { (showEditModal && selectedTask) && <AdminTaskEditModal task={selectedTask} users={users}
      onDismiss={onDoneEditing} studyEnvContext={studyEnvContext}/> }
  </div>
}

const MyTaskList = ({ studyEnvContext, taskData }: {studyEnvContext: StudyEnvContextT,
taskData: ParticipantTaskListDto}) => {
  const { user } = useUser()
  const [sorting, setSorting] = useState<SortingState>([
    { id: 'status', desc: true },
    { id: 'createdAt', desc: true }
  ])
  const myTasks = useMemo(() => taskData.tasks.filter(task =>
    task.assignedAdminUserId === user?.id || ''), [taskData.tasks.length])

  const columns: ColumnDef<ParticipantTask>[] = [{
    header: 'Task',
    id: 'taskDescription',
    cell: info => taskDescription(info.row.original, paramsFromContext(studyEnvContext), studyEnvContext, taskData)
  }, {
    header: 'Task Type',
    accessorKey: 'taskType',
    cell: info => renderTaskTypeColumn(info.row.original)
  }, {
    header: 'Status',
    accessorKey: 'status',
    cell: info => renderStatusColumn(info.row.original)
  }, {
    header: 'Created',
    accessorKey: 'createdAt',
    cell: info => instantToDefaultString(info.getValue() as number)
  }]

  const table = useReactTable({
    data: myTasks,
    state: { sorting },
    columns,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <>
    <h3 className="h4 mt-3">My tasks</h3>
    { basicTableLayout(table) }
    { renderEmptyMessage(myTasks, 'No tasks') }
  </>
}


const taskDescription = (
  task: ParticipantTask, studyEnvParams: StudyEnvParams,
  studyEnvContext: StudyEnvContextT, taskData: ParticipantTaskListDto
) => {
  const matchedNote = task.participantNoteId ?
    taskData.participantNotes.find(note => note.id === task.participantNoteId) : undefined
  const matchedEnrollee = task.enrolleeId ?
    taskData.enrollees.find(enrollee => enrollee.id === task.enrolleeId) : undefined
  const matchedForm = task.targetStableId ?studyEnvContext.currentEnv.configuredSurveys.find(survey =>
    survey.survey.stableId === task.targetStableId) : undefined

  return <>
    {matchedNote && matchedEnrollee && noteDescription(matchedNote, matchedEnrollee, studyEnvParams)}
    {matchedForm && matchedEnrollee && formDescription(matchedForm, matchedEnrollee, studyEnvParams)}
  </>
}

const noteDescription = (note: ParticipantNote, enrollee: Enrollee, studyEnvParams: StudyEnvParams) => {
  return <>
    <span>
      {_truncate(note.text, { length: 50 })}<br/>
      <span className="text-muted">Participant&nbsp;
        <Link to={studyEnvParticipantPath(studyEnvParams, enrollee?.shortcode as string)}>
          {enrollee?.shortcode}
        </Link>
      </span>
    </span>
  </>
}

const formDescription = (survey: StudyEnvironmentSurvey, enrollee: Enrollee, studyEnvParams: StudyEnvParams) => {
  return <>
    <span>
      {_truncate(survey.survey.name, { length: 50 })}
      <span className='text-muted fst-italic'> v{survey.survey.version}</span> <br/>
      <span className="text-muted">Participant&nbsp;
        <Link to={studyEnvParticipantPath(studyEnvParams, enrollee?.shortcode as string)}>
          {enrollee?.shortcode}
        </Link>
      </span>
    </span>
  </>
}

const renderStatusColumn = (task: ParticipantTask) => {
  if (task.status === 'COMPLETE') {
    return <div>
      <div><FontAwesomeIcon icon={faCheck}/> {instantToDateString(task.completedAt)} </div>
    </div>
  } else {
    return <span>{task.status}</span>
  }
}

const renderTaskTypeColumn = (task: ParticipantTask) => {
  if (task.taskType === 'ADMIN_NOTE') {
    return <span>Note</span>
  } else {
    return <span>Form</span>
  }
}
