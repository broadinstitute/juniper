import { findDefaultEnrollmentStudy } from './RedirectFromOAuth'
import { mockStudy, mockStudyEnv } from 'test-utils/test-portal-factory'
import { PortalStudy } from '@juniper/ui-core'


describe('determines default study', () => {
  it('find the default study if just one study', () => {
    const study = {
      ...mockStudy(),
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study }]
    expect(findDefaultEnrollmentStudy(null, portalStudies)).toBe(study)
  })

  it('does not find a default study if two studies', () => {
    const study = {
      ...mockStudy(),
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study }, { study }]
    expect(findDefaultEnrollmentStudy(null, portalStudies)).toBe(null)
  })

  it('finds a default study if only one is accepting enrollment', () => {
    const study = {
      ...mockStudy(),
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const inactiveStudy = {
      ...mockStudy(),
      studyEnvironments: [
        {
          ...mockStudyEnv(),
          studyEnvironmentConfig: {
            ...mockStudyEnv().studyEnvironmentConfig,
            acceptingEnrollment: false
          }
        }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study: inactiveStudy }, { study }]
    expect(findDefaultEnrollmentStudy(null, portalStudies)).toBe(study)
  })

  it('finds a study by shortcode', () => {
    const study = {
      ...mockStudy(),
      shortcode: 'foo',
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const study2 = {
      ...mockStudy(),
      shortcode: 'bar',
      studyEnvironments: [
        { ...mockStudyEnv() }
      ]
    }
    const portalStudies: PortalStudy[] = [{ study: study2 }, { study }]
    expect(findDefaultEnrollmentStudy('foo', portalStudies)).toBe(study)
  })
})
