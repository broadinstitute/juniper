import React, { useContext, useEffect } from 'react'
import { Enrollee, EnrolleeRelation, PortalParticipantUser, Profile } from 'api/api'
import { useUser } from './UserProvider'

export type ActiveUserContextT = {
  ppUser: PortalParticipantUser | null,
  profile: Profile | null,
  enrollees: Enrollee[],
  relations: EnrolleeRelation[],
  setActiveUser: (ppUserId: string) => void;
  updateProfile: (profile: Profile) => void;
}

/** current user object context */
const ActiveUserContext = React.createContext<ActiveUserContextT>({
  ppUser: null,
  profile: null,
  enrollees: [],
  relations: [],
  setActiveUser: () => {
    throw new Error('context not yet initialized')
  },
  updateProfile: () => {
    throw new Error('context not yet initialized')
  }
})

/**
 * Provides the currently active user - this could be the currently logged-in user,
 * or the user that is being proxied. For the logged-in ParticipantUser, regardless
 * of which user is being proxied, grab the `user` from the UserContext.
 */
export const useActiveUser = () => useContext(ActiveUserContext)

/** Provider for the current logged-in user. */
export default function ActiveUserProvider({ children }: { children: React.ReactNode }) {
  const userContext = useUser()
  const { ppUsers, enrollees, relations, updateEnrollee } = userContext

  const [activePpUser, setActivePpUser] = React.useState<PortalParticipantUser | null>(null)

  // When there are changes to the ppUsers (e.g., when initially logged in), find
  // an appropriate active user to set
  useEffect(() => {
    // if there is no active user, or the active user is not in the list of ppUsers,
    // then set the active user to the first user that has a enrollee that is a subject
    if (!activePpUser || !ppUsers.some(ppUser => ppUser.id === activePpUser.id)) {
      const ppUserWithSubject = ppUsers.find(
        ppUser => enrollees.some(
          enrollee => enrollee.profileId === ppUser.profileId && enrollee.subject))

      if (ppUserWithSubject) {
        setActivePpUser(ppUserWithSubject)
      } else if (ppUsers.length > 0) {
        setActivePpUser(ppUsers[0])
      }
    }
  }, [userContext.ppUsers])

  const context: ActiveUserContextT = {
    ppUser: activePpUser,
    profile:
      enrollees.find(enrollee => enrollee.profile && enrollee.profileId == activePpUser?.profileId)?.profile || null,
    enrollees: activePpUser ? enrollees.filter(enrollee => enrollee.profileId === activePpUser.profileId) : [],
    relations: activePpUser
      ? relations.filter(
        relation => enrollees.some(
          enrollee => enrollee.id === relation.targetEnrolleeId
            && enrollee.profileId === activePpUser.profileId))
      : [],
    setActiveUser: (ppUserId: string) => {
      const ppUser = ppUsers.find(ppUser => ppUser.id === ppUserId)
      if (ppUser) {
        setActivePpUser(ppUser)
      }
    },
    updateProfile: (profile: Profile) => {
      context.enrollees.forEach(enrollee => {
        enrollee.profile = profile
        updateEnrollee(enrollee)
      })
    }
  }

  return (
    <ActiveUserContext.Provider value={context}>
      {children}
    </ActiveUserContext.Provider>
  )
}
