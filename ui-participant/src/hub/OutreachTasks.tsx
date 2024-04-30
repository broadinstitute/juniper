import React, { useEffect, useState } from 'react'
import Api, { Enrollee, ParticipantTask, Study, SurveyResponse, TaskWithSurvey } from 'api/api'
import { getTaskPath } from './TaskLink'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import SurveyModal from './SurveyModal'
import { useTaskIdParam } from './survey/SurveyView'
import { useI18n } from '@juniper/ui-core'

type OutreachParams = {
    enrolleeShortcode?: string,
    studyShortcode?: string,
    stableId?: string
    version?: string
}

/** gets the outreach params from the URL */
const useOutreachParams = () => {
  const params = useParams<OutreachParams>()
  const taskId = useTaskIdParam()
  return {
    ...params,
    taskId,
    isOutreachPath: useLocation().pathname.includes('/outreach/') && !!taskId
  }
}

/** renders all outreach tasks for the set of enrollees. This operates on a list of enrollees, since
 * all of the tasks from the portal the user has signed into are shown, and that may include
 * multiple studies and therefore enrollees */
export default function OutreachTasks({ enrollees, studies }: {enrollees: Enrollee[], studies: Study[]}) {
  const { i18n } = useI18n()
  const navigate = useNavigate()
  const outreachParams = useOutreachParams()
  const [outreachTasks, setOutreachActivities] = useState<TaskWithSurvey[]>([])

  const sortedOutreachTasks = outreachTasks.sort((a, b) => {
    return a.task.createdAt - b.task.createdAt
  })
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

  const loadOutreachActivities = async () => {
    const outreachActivities = await Api.listOutreachActivities()
    setOutreachActivities(outreachActivities)
  }

  useEffect(() => {
    if (enrollees.length) {
      // the component may get rendered with zero enrollees during login/logout, don't bother fetching tasks then
      loadOutreachActivities()
    }
  }, [enrollees.map(enrollee => enrollee.shortcode).join(',')])

  useEffect(() => {
    const matchedTask = outreachTasks.find(({ task }) => task.id === outreachParams.taskId)?.task
    if (outreachParams.stableId && matchedTask && matchedTask.status === 'NEW') {
      const taskStudy = studyForTask(matchedTask, studies)
      const taskEnrollee = enrolleeForTask(matchedTask, enrollees)
      markTaskAsViewed(matchedTask, taskEnrollee, taskStudy)
    }
  }, [outreachParams.stableId])

  return <div className="">
    <div className="row g-3 pb-3">
      {sortedOutreachTasks.map(({ task, survey }) => {
        const taskStudy = studyForTask(task, studies)
        const taskEnrollee = enrolleeForTask(task, enrollees)
        const taskUrl = getTaskPath(task, taskEnrollee.shortcode, taskStudy.shortcode)
        // Gutters seem not to work??  So I had to add margins manually
        return <div className="col-md-6 col-sm-12" key={task.id}>
          <div className="p-4 d-block rounded-3 shadow-sm"
            style={{ background: '#fff', minHeight: '6em' }} key={task.id}>
            <h3 className="h5">{i18n(`${task.targetStableId}:${task.targetAssignedVersion}`, task.targetName)}</h3>
            <p className="text-muted">
              {survey.blurb}
            </p>
            <div className="py-3 text-center" style={{ background: 'var(--brand-color-shift-90)' }}>
              <Link to={taskUrl} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
                {i18n('outreachLearnMore')}
              </Link>
            </div>
          </div>
        </div>
      })}
      {outreachParams.isOutreachPath && <SurveyModal onDismiss={() => navigate('/hub')}/> }
    </div>
  </div>
}

/** finds the study that corresponds to the given task */
const studyForTask = (task: ParticipantTask, studies: Study[]) => {
  return studies.find(study => study.studyEnvironments[0].id === task.studyEnvironmentId)!
}

/** finds the enrollee that corresponds to the given task */
const enrolleeForTask = (task: ParticipantTask, enrollees: Enrollee[]) => {
  return enrollees.find(enrollee => enrollee.studyEnvironmentId === task.studyEnvironmentId)!
}


const foo = {
  'user': {
    'id': 'ff505f4a-a409-4a38-a069-0e872b3cf9d6',
    'createdAt': 1712940385.34359,
    'lastUpdatedAt': 1714483127.640095,
    'username': 'jsalk@test.com',
    'token': 'eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJlbWFpbCIjYTI1NjMwNDc2MDYifQ.',
    'lastLogin': 1714483127.640093,
    'loginAllowed': true,
    'withdrawn': false,
    'environmentName': 'sandbox',
    'portalParticipantUsers': [
      {
        'id': '6104ab35-a04a-447c-b1ed-4db44ff9190f',
        'createdAt': 1714409938.301945,
        'lastUpdatedAt': 1714409938.301945,
        'participantUserId': 'ff505f4a-a409-4a38-a069-0e872b3cf9d6',
        'portalEnvironmentId': '167a7ba2-820a-4ee1-ab47-9c060f9ec2f7',
        'profileId': 'd44978d9-39ae-438a-aa9d-22b302e8a8ba'
      }
    ]
  },
  'profile': {
    'id': 'd44978d9-39ae-438a-aa9d-22b302e8a8ba',
    'createdAt': 1714409938.30199,
    'lastUpdatedAt': 1714482124.749694,
    'givenName': 'Jonas',
    'familyName': 'Salk',
    'mailingAddress': {
      'id': '50684ab7-e948-4088-bf1e-6170a495f451',
      'createdAt': 1714409938.302014,
      'lastUpdatedAt': 1714482124.752927,
      'street1': '123 Walnut Street',
      'state': 'NY',
      'country': 'US',
      'city': 'New York',
      'postalCode': '10005'
    },
    'mailingAddressId': '50684ab7-e948-4088-bf1e-6170a495f451',
    'preferredLanguage': 'en',
    'contactEmail': 'jsalk@test.com',
    'doNotEmail': false,
    'doNotEmailSolicit': false,
    'sexAtBirth': 'male'
  },
  'ppUsers': [
    {
      'id': '6104ab35-a04a-447c-b1ed-4db44ff9190f',
      'createdAt': 1714409938.301945,
      'lastUpdatedAt': 1714409938.301945,
      'participantUserId': 'ff505f4a-a409-4a38-a069-0e872b3cf9d6',
      'portalEnvironmentId': '167a7ba2-820a-4ee1-ab47-9c060f9ec2f7',
      'profileId': 'd44978d9-39ae-438a-aa9d-22b302e8a8ba'
    }
  ],
  'enrollees': [
    {
      'id': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
      'createdAt': 1714222739.08519,
      'lastUpdatedAt': 1714409939.07551,
      'participantUserId': 'ff505f4a-a409-4a38-a069-0e872b3cf9d6',
      'profileId': 'd44978d9-39ae-438a-aa9d-22b302e8a8ba',
      'profile': {
        'id': 'd44978d9-39ae-438a-aa9d-22b302e8a8ba',
        'createdAt': 1714409938.30199,
        'lastUpdatedAt': 1714482124.749694,
        'givenName': 'Jonas',
        'familyName': 'Salk',
        'mailingAddress': {
          'id': '50684ab7-e948-4088-bf1e-6170a495f451',
          'createdAt': 1714409938.302014,
          'lastUpdatedAt': 1714482124.752927,
          'street1': '123 Walnut Street',
          'state': 'NY',
          'country': 'US',
          'city': 'New York',
          'postalCode': '10005'
        },
        'mailingAddressId': '50684ab7-e948-4088-bf1e-6170a495f451',
        'preferredLanguage': 'en',
        'contactEmail': 'jsalk@test.com',
        'doNotEmail': false,
        'doNotEmailSolicit': false,
        'sexAtBirth': 'male'
      },
      'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
      'preEnrollmentResponseId': 'ea88d42a-684c-466a-aa99-cf8a0794bf06',
      'shortcode': 'HDSALK',
      'subject': true,
      'consented': true,
      'surveyResponses': [],
      'consentResponses': [],
      'participantTasks': [
        {
          'id': '8e358a5a-3768-47d5-8cf3-bd28171d27b9',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409938.884251,
          'status': 'NEW',
          'taskType': 'SURVEY',
          'targetName': 'Family History',
          'targetStableId': 'hd_hd_famHx',
          'targetAssignedVersion': 1,
          'taskOrder': 3,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f'
        },
        {
          'id': '1e9d359d-4faa-4a31-8d9b-51a4294c5beb',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409938.88701,
          'status': 'NEW',
          'taskType': 'SURVEY',
          'targetName': 'Lifestyle',
          'targetStableId': 'hd_hd_lifestyle',
          'targetAssignedVersion': 1,
          'taskOrder': 5,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f'
        },
        {
          'id': '9c378b6a-4d65-4815-a628-145f96ae5f7b',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409938.889698,
          'status': 'NEW',
          'taskType': 'SURVEY',
          'targetName': 'Other Medical History',
          'targetStableId': 'hd_hd_medHx',
          'targetAssignedVersion': 1,
          'taskOrder': 2,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f'
        },
        {
          'id': '3a37b0b3-6fbd-421b-8f72-55a409b67ee6',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409938.894424,
          'status': 'NEW',
          'taskType': 'SURVEY',
          'targetName': 'Medications',
          'targetStableId': 'hd_hd_medList',
          'targetAssignedVersion': 1,
          'taskOrder': 4,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f'
        },
        {
          'id': 'd4c268aa-743c-4adb-85b3-70f915c34702',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409938.926495,
          'completedAt': 1714240738.934624,
          'status': 'COMPLETE',
          'taskType': 'SURVEY',
          'targetName': 'Mental Health',
          'targetStableId': 'hd_hd_mental',
          'targetAssignedVersion': 1,
          'taskOrder': 6,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f',
          'surveyResponseId': '5d127619-7d96-496f-8b3c-f7d4428d4337'
        },
        {
          'id': '5e4ac962-b2f5-4ed5-bf49-be050c57e30d',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409938.964866,
          'completedAt': 1714237138.974558,
          'status': 'COMPLETE',
          'taskType': 'SURVEY',
          'targetName': 'The Basics',
          'targetStableId': 'hd_hd_basicInfo',
          'targetAssignedVersion': 1,
          'taskOrder': 0,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f',
          'surveyResponseId': 'bc9bd77e-65c5-4665-a4e7-ea96136a2111'
        },
        {
          'id': '7d632a69-0bef-4ef9-8354-49a8d7b56645',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409938.992315,
          'completedAt': 1714237139.00194,
          'status': 'COMPLETE',
          'taskType': 'SURVEY',
          'targetName': 'Cardiometabolic Medical History',
          'targetStableId': 'hd_hd_cardioHx',
          'targetAssignedVersion': 1,
          'taskOrder': 1,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f',
          'surveyResponseId': '59656b8c-0736-40ee-898e-6e5621ad3fa9'
        },
        {
          'id': 'bcee33ef-5247-40ef-b97b-80b7337dd9cf',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409939.02332,
          'completedAt': 1714244339.034689,
          'status': 'COMPLETE',
          'taskType': 'SURVEY',
          'targetName': 'Social Determinants of Health',
          'targetStableId': 'hd_hd_socialHealth',
          'targetAssignedVersion': 3,
          'taskOrder': 9,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f',
          'surveyResponseId': '466af0dc-1fd4-4e87-a62d-4e8ea99d2ebb'
        },
        {
          'id': 'c318b4f0-78c7-49e7-913e-c1c15b8c7d69',
          'createdAt': 1714222739.08519,
          'lastUpdatedAt': 1714409939.062665,
          'completedAt': 1714409939.059123,
          'status': 'COMPLETE',
          'taskType': 'CONSENT',
          'targetName': 'OurHealth Consent',
          'targetStableId': 'hd_hd_consent',
          'targetAssignedVersion': 1,
          'taskOrder': 11,
          'blocksHub': false,
          'studyEnvironmentId': '5f5e25d5-ae5f-4274-bf2a-f9cdf498a44e',
          'enrolleeId': '4e7f6a4b-97c2-4ec1-88f7-b8b0101d82cb',
          'portalParticipantUserId': '6104ab35-a04a-447c-b1ed-4db44ff9190f',
          'surveyResponseId': '3970370c-5ace-4c3f-a2c0-618752f2b6e0'
        }
      ],
      'participantNotes': [],
      'kitRequests': [
        {
          'id': '6bde1dee-8945-4ae2-8ab5-337d211102f5',
          'createdAt': 1714409938.834669,
          'kitType': {
            'id': '201b258f-f078-4d04-99ca-709eff234d68',
            'createdAt': 1712940361.591533,
            'lastUpdatedAt': 1712940361.591533,
            'name': 'SALIVA',
            'displayName': 'Saliva',
            'description': 'Saliva sample collection kit'
          },
          'status': 'CREATED',
          'sentToAddress': '{"firstName":"Jona0005","country":"US"}',
          'skipAddressValidation': false,
          'details': '{"requestId":"6bde1dee-8945-4ae2-8ab5-337d'
        }
      ],
      'relations': []
    }
  ],
  'relations': []
}
