import React, {
  useContext,
  useEffect,
  useState
} from 'react'
import Api, {
  LocalSiteContent,
  Portal,
  PortalEnvironment
} from 'api/api'
import { SUPPORT_EMAIL_ADDRESS } from '@juniper/ui-core'
import { useReturnToLanguage } from 'browserPersistentState'


/** current portal object context */
const PortalContext = React.createContext<PortalEnvContextT | null>(null)

export type PortalEnvContextT = {
  portal: Portal,
  portalEnv: PortalEnvironment,
  loadedLanguage: string | null,
  reloadPortal: (languageCode?: string) => void,
  localContent: LocalSiteContent
}

/** use the loaded portal.  Attempting to call this outside of PortalProvider children will throw an exception */
export function usePortalEnv(): PortalEnvContextT {
  const portalContext = useContext(PortalContext)
  if (!portalContext?.portal) {
    throw ('Portal environment not initialized')
  }
  // the api guarantees the first environment and first localizedSiteContents returned are the correct ones
  const portalEnv = portalContext.portal.portalEnvironments[0]
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  const localContent = portalEnv.siteContent!.localizedSiteContents[0]

  return {
    portal: portalContext.portal,
    portalEnv,
    reloadPortal: portalContext.reloadPortal,
    localContent,
    loadedLanguage: portalContext.loadedLanguage
  }
}

/**
 * Provider for the current user object.
 * if a user object has already been obtained, it can be passed-in
 */
export default function PortalProvider({ children }: { children: React.ReactNode }) {
  const [envState, setEnvState] = useState<Portal | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)
  const [returnToLanguage] = useReturnToLanguage()

  useEffect(() => {
    reloadPortal()
  }, [])

  const updateWebManifest = (portal: Portal) => {
    const manifest = {
      'name': portal.name,
      'short_name': portal.name,
      'description': '',
      'start_url': '.',
      'background_color': '#ffffff',
      'theme_color': '#000000',
      'icons': [{
        'src': 'favicon.ico',
        'sizes': '64x64 32x32 24x24 16x16',
        'type': 'image/x-icon'
      }]
    }
    const stringManifest = JSON.stringify(manifest)
    const blob = new Blob([stringManifest], { type: 'application/json' })
    const manifestURL = URL.createObjectURL(blob)
    document.querySelector('#juniper-manifest')?.setAttribute('href', manifestURL)
  }

  const reloadPortal = (languageCode?: string) => {
    const savedLanguage = localStorage.getItem('selectedLanguage') || returnToLanguage
    const selectedLanguage = languageCode || (savedLanguage === null ? undefined : savedLanguage)
    setIsLoading(true)
    // the ! below is unnecessary and wrong, but Vite ts validation insists on it?
    Api.getPortal(selectedLanguage!).then(result => {
      setEnvState(result)
      updateWebManifest(result)
      setIsError(false)
      setIsLoading(false)
    }).catch(() => {
      setIsError(true)
      setIsLoading(false)
    })
  }

  const portalEnv = envState && {
    portal: envState,
    portalEnv: envState.portalEnvironments[0],
    reloadPortal,
    localContent: envState.portalEnvironments[0].siteContent!.localizedSiteContents[0],
    loadedLanguage: envState.portalEnvironments[0].siteContent!.localizedSiteContents[0].language
  }

  return <>
    {isLoading && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle">Loading...</div>
    </div>}
    {isError && <div className="bg-white h-100 w-100">
      <div className="position-absolute top-50 start-50 translate-middle text-center">
        There is no Juniper site configured for this url.<br/>
        If this is an error, contact <a href={`mailto:${SUPPORT_EMAIL_ADDRESS}`}>{SUPPORT_EMAIL_ADDRESS}</a>.
      </div>
    </div>}
    {!isLoading && !isError && <PortalContext.Provider value={portalEnv}>
      {children}
    </PortalContext.Provider>}
  </>
}
