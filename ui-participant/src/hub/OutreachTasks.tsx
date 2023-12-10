import React, {useEffect} from 'react'
import Api, {Enrollee, ParticipantTask, Study, SurveyResponse} from "../api/api";
import {getTaskPath} from "./TaskLink";
import {Link, useLocation, useNavigate, useParams, useSearchParams} from "react-router-dom";
import SurveyModal from "./SurveyModal";
import {useTaskIdParam} from "./survey/SurveyView";
import {getResumeData, getUpdatedAnswers} from "../util/surveyJsUtils";

type OutreachParams = {
    enrolleeShortcode?: string,
    studyShortcode?: string,
    stableId?: string
    version?: string
}

const useOutreachParams = () => {
    const params = useParams<OutreachParams>()
    const taskId = useTaskIdParam()
    return {
        ...params,
        taskId,
        isOutreachPath: useLocation().pathname.includes('/outreach/')
    }
}

export default function OutreachTasks({enrollees, studies}: {enrollees: Enrollee[], studies: Study[]}) {
    const navigate = useNavigate()
    const outreachParams = useOutreachParams()

    const outreachTasks = enrollees.flatMap(enrollee => enrollee.participantTasks)
        .filter(task => task.taskType === 'OUTREACH')
        .sort((a, b) => a.createdAt - b.createdAt)
    const markTaskAsViewed = async (task: ParticipantTask, enrollee: Enrollee, study: Study) => {
        const responseDto = {
            resumeData: '{}',
            enrolleeId: enrollee.id,
            answers: [],
            creatingParticipantId: enrollee.participantUserId,
            surveyId: task.id,
            complete: false
        } as SurveyResponse
        await Api.updateSurveyResponse({
            studyShortcode: study.shortcode,
            enrolleeShortcode: enrollee.shortcode,
            stableId: task.targetStableId,
            version: task.targetAssignedVersion,
            taskId: task.id,
            alertErrors: false,
            response: responseDto
        })
    }

    useEffect(() => {
        const matchedTask = outreachTasks.find(task => task.id === outreachParams.taskId)
        if (outreachParams.stableId && matchedTask && matchedTask.status === 'NEW') {
            const taskStudy = studyForTask(matchedTask, studies)
            const taskEnrollee = enrolleeForTask(matchedTask, enrollees)
            markTaskAsViewed(matchedTask, taskEnrollee, taskStudy)
        }
    }, [outreachParams.stableId])

    return <div className="">
        <div className="row g-3">
            {outreachTasks.map(task => {
                const taskStudy = studyForTask(task, studies)
                const taskEnrollee = enrolleeForTask(task, enrollees)
                const taskUrl = getTaskPath(task, taskEnrollee.shortcode, taskStudy.shortcode)
                // Gutters seem not to work??  So I had to add margins manually
                return <div className="col-md-4 col-sm-12">
                        <Link to={taskUrl} className="text-center pt-2 d-block rounded-3"
                    style={{background: '#fff', minHeight: '6em'}} key={task.id}>
                        <h3 className="h5">{task.targetName}</h3>
                        { /* blurb will go here */}
                    </Link>
                </div>
            })}
            {outreachParams.isOutreachPath && <SurveyModal onDismiss={() => navigate('/hub')}/> }
        </div>
    </div>

}

const studyForTask = (task: ParticipantTask, studies: Study[]) => {
    return studies.find(study => study.studyEnvironments[0].id === task.studyEnvironmentId)!
}

const enrolleeForTask = (task: ParticipantTask, enrollees: Enrollee[]) => {
    return enrollees.find(enrollee => enrollee.studyEnvironmentId === task.studyEnvironmentId)!
}