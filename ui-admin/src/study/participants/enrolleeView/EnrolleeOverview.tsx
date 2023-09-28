import React from 'react'
import {Enrollee} from "api/api";
import {StudyEnvContextT} from "../../StudyEnvironmentRouter";
import ParticipantNotesView from "./ParticipantNotesView";
import {dateToDefaultString} from "util/timeUtils";
import KitRequests from "../KitRequests";

export default function EnrolleeOverview({ enrollee, studyEnvContext, onUpdate }:
        {enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
    return <div>
        <div className="mb-5">
            <label className="form-label">
                Given name: <input className="form-control" type="text"
                                   readOnly={true} value={enrollee.profile.givenName}/>
            </label>
            <label className="form-label ms-2">
                Family name: <input className="form-control" type="text"
                                    readOnly={true} value={enrollee.profile.familyName}/>
            </label>
            <label className="form-label ms-2">
                Birthdate:
                <input className="form-control" type="text"
                       readOnly={true} value={dateToDefaultString(enrollee.profile.birthDate)}/>
            </label>
        </div>
        <div className="mb-5">
            <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
                                  studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
        </div>


        <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
    </div>
}