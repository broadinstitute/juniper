import React, {useState} from 'react'
import {AdminTask, AdminUser} from "api/api";
import {Modal} from "react-bootstrap";
import Select from "react-select";
import LoadingSpinner from "util/LoadingSpinner";
import AdminUserSelect from "user/AdminUserSelect";


export default function AdminTaskEditor({task, workingTask, setWorkingTask, users}:
{ task: AdminTask, workingTask: AdminTask, setWorkingTask: (task: AdminTask) => void, users: AdminUser[]}) {
    const selectedUser = users.find(user => user.id === workingTask.assignedAdminUserId)
    const setSelectedUser = ((user: AdminUser | undefined) => {
        setWorkingTask({
            ...workingTask,
            assignedAdminUserId: user?.id
        })
    })
    return <div>
        <AdminUserSelect selectedUser={selectedUser} setSelectedUser={setSelectedUser} users={users}/>
    </div>
}

export const AdminTaskEditModal = ({task, users, onDismiss}:
                                       { task: AdminTask, users: AdminUser[], onDismiss: () => void}) => {
    const [workingTask, setWorkingTask] = useState<AdminTask>(task)
    const [isLoading, setIsLoading] = useState(false)

    const saveTask = () => {
        alert('not yet implemented')
    }

    return <Modal show={true} onHide={onDismiss}>
        <Modal.Header closeButton>
            <Modal.Title>Edit admin task</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <AdminTaskEditor task={task} workingTask={workingTask} setWorkingTask={setWorkingTask} users={users}/>
        </Modal.Body>
        <Modal.Footer>
            <LoadingSpinner isLoading={isLoading}>
                <button className="btn btn-primary" onClick={saveTask}>Save</button>
                <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
            </LoadingSpinner>
        </Modal.Footer>
    </Modal>
}