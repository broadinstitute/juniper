import React from 'react'
import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import { EnrolleeLink } from 'study/families/FamilyOverview'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'

/**
 *
 */
export const FamilyMembersList = (
  { family, studyEnvContext }: { family: Family, studyEnvContext: StudyEnvContextT }) => {
  const flattenFamilyMembers = (family: Family): FamilyMemberWithRelation[] => {
    return family.members?.map(
      member => {
        return {
          member,
          relations: family.relations?.filter(r => r.enrolleeId === member.id).reduce<FamilyRelation[]>((acc, r) => {
            if (acc.find(a => a.relation === r.familyRelationship)) {
              return acc.map(a => a.relation === r.familyRelationship ? {
                ...a,
                targets: a.targets.concat(family.members?.find(m => m.id === r.targetEnrolleeId) || [])
              } : a)
            }
            return acc.concat({
              relation: r.familyRelationship,
              targets: [family.members?.find(m => m.id === r.targetEnrolleeId) || []]
            })
          }, []) || []
        }
      }
    ) || []
  }

  return <>
    {flattenFamilyMembers(family).map((fm, index) => <div key={index}>
      <EnrolleeLink enrollee={fm.member} studyEnvContext={studyEnvContext} />
      <ul className={'m-0 mb-1'}>
        {fm.relations.map((r, index) => <li key={index}>
          {r.relation} of {r.targets.map(t => `${t.profile?.givenName} ${t.profile?.familyName}`).join(', ')}
        </li>)}
      </ul>
    </div>)
    }
  </>
}

type FamilyRelation = {
  relation: string,
    targets: Enrollee[]
}

type FamilyMemberWithRelation = {
  member: Enrollee,
  relations: FamilyRelation[]
}
