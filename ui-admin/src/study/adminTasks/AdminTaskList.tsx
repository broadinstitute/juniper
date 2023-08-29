import React, { useEffect, useState } from 'react'
import Api, {AdminTask, AdminTaskListDto} from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
    ColumnDef,
    getCoreRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable
} from '@tanstack/react-table'
import { basicTableLayout, IndeterminateCheckbox } from 'util/tableUtils'
import { instantToDefaultString } from 'util/timeUtils'
import {paramsFromContext, StudyEnvContextT, StudyEnvParams} from "../StudyEnvironmentRouter";
import {useAdminUserContext} from "providers/AdminUserProvider";
import _truncate from 'lodash/truncate'
import {studyEnvParticipantPath} from "../participants/ParticipantsRouter";
import {Link} from "react-router-dom";



/** show the mailing list in table */
export default function AdminTaskList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
    const [taskData, setTaskData] = useState<AdminTaskListDto>({
        tasks: [], enrollees: [], participantNotes: []
    })
    const [isLoading, setIsLoading] = useState(true)
    const [sorting, setSorting] = React.useState<SortingState>([])
    const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
    const { users } = useAdminUserContext()

    const columns: ColumnDef<AdminTask>[] = [{
        id: 'select',
        header: ({ table }) => <IndeterminateCheckbox
            checked={table.getIsAllRowsSelected()} indeterminate={table.getIsSomeRowsSelected()}
            onChange={table.getToggleAllRowsSelectedHandler()}/>,
        cell: ({ row }) => (
            <div className="px-1">
                <IndeterminateCheckbox
                    checked={row.getIsSelected()} indeterminate={row.getIsSomeSelected()}
                    onChange={row.getToggleSelectedHandler()} disabled={!row.getCanSelect()}/>
            </div>
        )
    }, {
        header: 'Created',
        accessorKey: 'createdAt',
        cell: info => instantToDefaultString(info.getValue() as number)
    }, {
        header: 'Assignee',
        accessorKey: 'assignedAdminUserId',
        cell: info => users.find(user => user.id === info.getValue())?.username
    }, {
        header: 'Type',
        accessorKey: 'taskType'
    }, {
        header: 'Task',
        id: 'taskDescription',
        cell: info => taskDescription(info.row.original, paramsFromContext(studyEnvContext), taskData)
    }]


    const table = useReactTable({
        data: taskData.tasks,
        columns,
        state: {
            sorting,
            rowSelection
        },
        enableRowSelection: true,
        onSortingChange: setSorting,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        onRowSelectionChange: setRowSelection,
        debugTable: true
    })

    useEffect(() => {
        setIsLoading(true)
        Api.fetchAdminTasksByStudyEnv(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
            studyEnvContext.currentEnv.environmentName, ['enrollee', 'participantNote']).then(result => {
            setTaskData(result)
            setIsLoading(false)
        }).catch((e: Error) => {
            alert(`error loading mailing list ${  e}`)
            setIsLoading(false)
        })
    }, [studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName])
    return <div className="container p-3">
        <h1 className="h4">Admin tasks</h1>
        <LoadingSpinner isLoading={isLoading}>
            {basicTableLayout(table)}
        </LoadingSpinner>
    </div>
}


const taskDescription = (task: AdminTask, studyEnvParams: StudyEnvParams, taskData: AdminTaskListDto) => {
    const matchedNote = task.participantNoteId ?
        taskData.participantNotes.find(note => note.id === task.participantNoteId) : undefined
    const matchedEnrollee = task.enrolleeId ?
        taskData.enrollees.find(enrollee => enrollee.id === task.enrolleeId) : undefined
    return <>
        {matchedNote && <span>
            {_truncate(matchedNote.text, {length: 50})}<br/>
            <span className="text-muted">Participant&nbsp;
                <Link to={studyEnvParticipantPath(studyEnvParams, matchedEnrollee?.shortcode as string)}>
                    {matchedEnrollee?.shortcode}
                </Link>
            </span>
        </span> }
    </>
}