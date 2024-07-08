import React, { useState } from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { useSearchParams } from 'react-router-dom'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import ParticipantSearch from './search/ParticipantSearch'
import { useParticipantSearchState } from 'util/participantSearchUtils'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import ParticipantListGroupedByFamily from 'study/participants/participantList/ParticipantListGroupedByFamily'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faPeopleGroup,
  faPerson
} from '@fortawesome/free-solid-svg-icons'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])


  const [searchParams, setSearchParams] = useSearchParams()

  const groupByFamily = searchParams.get('groupByFamily') === 'true'
  const setGroupByFamily = (groupByFamily: boolean) => {
    setSearchParams(params => {
      console.log(Array.from(params.entries()).reduce(
        (o, [key, value]) => ({ ...o, [key]: value }),
        {}
      ))
      return {
        ...Array.from(params.entries()).reduce(
          (o, [key, value]) => ({ ...o, [key]: value }),
          {}
        ),
        groupByFamily: groupByFamily.toString()
      }
    })
  }

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  const {
    searchState,
    updateSearchState,
    setSearchState,
    searchExpression
  } = useParticipantSearchState({ searchParams, setSearchParams })

  const { isLoading } = useLoadingEffect(async () => {
    const results = await Api.executeSearchExpression(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      // if families exist, adding these expression guarantees that we get all families.
      // might be a better way to do it, but this works for now
      familyLinkageEnabled
        ? concatSearchExpressions([`(include({family.shortcode}))`, searchExpression])
        : searchExpression)
    setParticipantList(results)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName, searchExpression])


  return <div className="ParticipantList container-fluid px-4 py-2">
    {renderPageHeader('Participant List')}
    <div className="d-flex align-content-center">
      <ParticipantSearch
        key={currentEnv.environmentName}
        studyEnvContext={studyEnvContext}
        searchState={searchState}
        updateSearchState={updateSearchState}
        setSearchState={setSearchState}
      />
      {
        familyLinkageEnabled && <div className="d-flex align-content-center ms-2">
          <button className="btn-secondary btn-sm"
            onClick={() => setGroupByFamily(!groupByFamily)}>
            {groupByFamily
              ? <><FontAwesomeIcon size={'sm'} icon={faPerson}/> Ungroup Families</>
              : <><FontAwesomeIcon size={'sm'} icon={faPeopleGroup}/> Group by family</>}
          </button>
        </div>
      }

    </div>


    <LoadingSpinner isLoading={isLoading}>
      {groupByFamily
        ? <ParticipantListGroupedByFamily participantList={participantList} studyEnvContext={studyEnvContext}/>
        : <ParticipantListTable participantList={participantList} studyEnvContext={studyEnvContext}/>}
    </LoadingSpinner>
  </div>
}


export default ParticipantList
