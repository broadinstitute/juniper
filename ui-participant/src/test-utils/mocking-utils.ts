import { Config, EnvSpec } from '../api/api'

/**
 * Returns a mock B2C Config
 */
export const mockB2cConfig: () => Config = () => ({
  b2cPolicyName: 'testPolicyName',
  b2cTenantName: 'testTenantName',
  b2cClientId: 'testClientId',
  b2cChangePasswordPolicyName: 'testChangePasswordPolicyName'
})

/**
 * Returns a mock EnvSpec
 */
export const mockEnvSpec: () => EnvSpec = () => ({
  envName: 'sandbox',
  shortcodeOrHostname: 'foo',
  shortcode: 'testShortcode'
})
