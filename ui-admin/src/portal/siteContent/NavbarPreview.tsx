import React from 'react'
import {
  EnrolleeRelation,
  LocalSiteContent,
  ParticipantNavbar,
  ParticipantUser,
  Portal,
  PortalEnvironment,
  Profile
} from '@juniper/ui-core'

export function NavbarPreview(
  {
    portal,
    portalEnv,
    localContent,
    user,
    profile,
    proxyRelations
  } : {
    portal: Portal,
    portalEnv: PortalEnvironment,
    localContent: LocalSiteContent,
    user?: ParticipantUser,
    profile?: Profile,
    proxyRelations?: EnrolleeRelation[]
  }) {
  return <div>
    <ParticipantNavbar
      portal={portal}
      portalEnv={portalEnv}
      localContent={localContent}
      user={user}
      profile={profile}
      proxyRelations={proxyRelations || []}
      reloadPortal={() => {
      }}
      updatePreferredLanguage={async () => {
      }}
      doChangePassword={() => {
      }}
      doLogout={() => {
      }}
    />
  </div>
}
