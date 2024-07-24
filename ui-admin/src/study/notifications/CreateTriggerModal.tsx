
import React, { useState } from 'react'
import { StudyEnvParams, Trigger } from '@juniper/ui-core'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { Button } from 'components/forms/Button'
import { generateStableId } from 'util/juniperSurveyUtils'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import TriggerBaseForm from './TriggerBaseForm'

/** gets a default config -- eventually we'll want this to load from a template study instead of being in source code */
const getDefaultConfig = (): Trigger => {
  return {
    id: '',
    triggerType: 'EVENT',
    deliveryType: 'EMAIL',
    eventType: 'STUDY_ENROLLMENT',
    actionType: 'NOTIFICATION',
    actionScope: 'STUDY',
    active: true,
    portalEnvironmentId: '',
    studyEnvironmentId: '',
    afterMinutesIncomplete: 72 * 60,
    reminderIntervalMinutes: 72 * 60,
    maxNumReminders: 3,
    emailTemplate: {
      stableId: 'placeholderStableId',
      name: '',
      version: 1,
      localizedEmailTemplates: [{ language: 'en', subject: 'Insert subject', body: EMAIL_TEMPLATE }]
    },
    emailTemplateId: ''
  }
}

/** Modal for a new notification config -- only specifies the basic attributes */
export default function CreateTriggerModal({ studyEnvParams, onDismiss, onCreate }:
{studyEnvParams: StudyEnvParams, onDismiss: () => void, onCreate: (config: Trigger) => void}) {
  const [config, setConfig] = React.useState<Trigger>(getDefaultConfig())
  const [isLoading, setIsLoading] = useState(false)
  const localizedEmailTemplate = config.emailTemplate.localizedEmailTemplates[0]

  const createConfig = async () => {
    doApiLoad(async () => {
      const savedConfig = await Api.createTrigger(studyEnvParams, config)
      Store.addNotification(successNotification('Notification created'))
      onCreate(savedConfig)
    }, { setIsLoading })
  }

  // the email template stableId is the portalShortcode plus sanitized name
  const generateTemplateStableId = (name: string) => {
    return `${studyEnvParams.portalShortcode  }_${  generateStableId(name)}`
  }

  return <Modal show={true} className="modal-lg" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create Notification</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form className="bg-white p-3 my-2">
        <label className="form-label" htmlFor="templateName">
                    Notification name (this name is only for staff use -- participants will not see it)
        </label>
        <input type="text" size={20} id="templateName" className="form-control mb-3"
          value={config.emailTemplate.name}
          onChange={e => setConfig({
            ...config,
            emailTemplate: {
              ...config.emailTemplate,
              name: e.target.value,
              stableId: generateTemplateStableId(e.target.value),
              localizedEmailTemplates: [{ ...localizedEmailTemplate }]
            }
          })}/>
        <TriggerBaseForm trigger={config} setTrigger={setConfig}/>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <Button variant="primary"
          disabled={!config.emailTemplate.name}
          onClick={createConfig}
        >Create</Button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

/* eslint-disable max-len */
const EMAIL_TEMPLATE = `
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional //EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
<head>
    <!--[if gte mso 9]>
    <xml>
        <o:OfficeDocumentSettings>
            <o:AllowPNG/>
            <o:PixelsPerInch>96</o:PixelsPerInch>
        </o:OfficeDocumentSettings>
    </xml>
    <![endif]-->
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="x-apple-disable-message-reformatting">
    <!--[if !mso]><!--><meta http-equiv="X-UA-Compatible" content="IE=edge"><!--<![endif]-->
    <title></title>

    <style type="text/css">
        @media only screen and (min-width: 620px) {
            .u-row {
                width: 600px !important;
            }
            .u-row .u-col {
                vertical-align: top;
            }

            .u-row .u-col-100 {
                width: 600px !important;
            }

        }

        @media (max-width: 620px) {
            .u-row-container {
                max-width: 100% !important;
                padding-left: 0px !important;
                padding-right: 0px !important;
            }
            .u-row .u-col {
                min-width: 320px !important;
                max-width: 100% !important;
                display: block !important;
            }
            .u-row {
                width: 100% !important;
            }
            .u-col {
                width: 100% !important;
            }
            .u-col > div {
                margin: 0 auto;
            }
        }
        body {
            margin: 0;
            padding: 0;
        }

        table,
        tr,
        td {
            vertical-align: top;
            border-collapse: collapse;
        }

        p {
            margin: 0;
        }

        .ie-container table,
        .mso-container table {
            table-layout: fixed;
        }

        * {
            line-height: inherit;
        }

        a[x-apple-data-detectors='true'] {
            color: inherit !important;
            text-decoration: none !important;
        }

        table, td { color: #000000; } #u_body a { color: #9b2485; text-decoration: underline; } @media (max-width: 480px) { #u_content_image_1 .v-src-width { width: auto !important; } #u_content_image_1 .v-src-max-width { max-width: 87% !important; } #u_content_text_2 .v-container-padding-padding { padding: 20px 20px 10px !important; } #u_content_text_3 .v-container-padding-padding { padding: 10px 20px !important; } #u_content_divider_6 .v-container-padding-padding { padding: 20px 20px 10px !important; } #u_content_text_deprecated_1 .v-container-padding-padding { padding: 20px !important; } #u_content_text_4 .v-container-padding-padding { padding: 20px 20px 10px !important; } }
    </style>



    <!--[if !mso]><!--><link href="https://fonts.googleapis.com/css?family=Montserrat:400,700&display=swap" rel="stylesheet" type="text/css"><!--<![endif]-->

</head>

<body class="clean-body u_body" style="margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #eeeeee;color: #000000">
<!--[if IE]><div class="ie-container"><![endif]-->
<!--[if mso]><div class="mso-container"><![endif]-->
<table id="u_body" style="border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #eeeeee;width:100%" cellpadding="0" cellspacing="0">
    <tbody>

    <tr style="vertical-align: top">
        <td style="word-break: break-word;border-collapse: collapse !important;vertical-align: top">
            <!--[if (mso)|(IE)]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td align="center" style="background-color: #eeeeee;"><![endif]-->


            <div class="u-row-container" style="padding: 0px;background-color: transparent">
                <div class="u-row" style="Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;">
                    <div style="border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;">
                        <!--[if (mso)|(IE)]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td style="padding: 0px;background-color: transparent;" align="center"><table cellpadding="0" cellspacing="0" border="0" style="width:600px;"><tr style="background-color: transparent;"><![endif]-->

                        <!--[if (mso)|(IE)]><td align="center" width="600" style="background-color: #ffffff;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;" valign="top"><![endif]-->
                        <div class="u-col u-col-100" style="max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;">
                            <div style="background-color: #ffffff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;">
                                <!--[if (!mso)&(!IE)]><!--><div style="box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;"><!--<![endif]-->

                                <table id="u_content_image_1" style="font-family:'Montserrat',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                                    <tbody>
                                    <tr>
                                        <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:40px 0px 20px;font-family:'Montserrat',sans-serif;" align="left">

                                            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                                <tr>
                                                    <td style="padding-right: 0px;padding-left: 0px;" align="center">

                                                        <img align="center" border="0" src="\${siteMediaBaseUrl}/1/email_logo.webp" alt="OurHealth logo" style="outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 34%;max-width: 204px;" width="204" class="v-src-width v-src-max-width"/>

                                                    </td>
                                                </tr>
                                            </table>

                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                                <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->
                            </div>
                        </div>
                        <!--[if (mso)|(IE)]></td><![endif]-->
                        <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->
                    </div>
                </div>
            </div>



            <div class="u-row-container" style="padding: 0px;background-color: transparent">
                <div class="u-row" style="Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;">
                    <div style="border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;">
                        <!--[if (mso)|(IE)]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td style="padding: 0px;background-color: transparent;" align="center"><table cellpadding="0" cellspacing="0" border="0" style="width:600px;"><tr style="background-color: transparent;"><![endif]-->

                        <!--[if (mso)|(IE)]><td align="center" width="600" style="background-color: #ffffff;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;" valign="top"><![endif]-->
                        <div class="u-col u-col-100" style="max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;">
                            <div style="background-color: #ffffff;height: 100%;width: 100% !important;">
                                <!--[if (!mso)&(!IE)]><!--><div style="box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;"><!--<![endif]-->

                                <table id="u_content_text_2" style="font-family:'Montserrat',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                                    <tbody>
                                    <tr>
                                        <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:10px 50px;font-family:'Montserrat',sans-serif;" align="left">

                                            <div style="font-family: 'Montserrat',sans-serif; font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;">
                                                <p style="line-height: 140%;">Hello.</p>
                                                <p style="line-height: 140%;">&nbsp;This is an email from us </p>
                                                <p style="line-height: 140%;">Here is some more text.</p>
                                            </div>

                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                                <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->
                            </div>
                        </div>
                        <!--[if (mso)|(IE)]></td><![endif]-->
                        <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->
                    </div>
                </div>
            </div>



            <div class="u-row-container" style="padding: 0px;background-color: transparent">
                <div class="u-row" style="Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;">
                    <div style="border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;">
                        <!--[if (mso)|(IE)]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td style="padding: 0px;background-color: transparent;" align="center"><table cellpadding="0" cellspacing="0" border="0" style="width:600px;"><tr style="background-color: transparent;"><![endif]-->

                        <!--[if (mso)|(IE)]><td align="center" width="600" style="background-color: #ffffff;width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;" valign="top"><![endif]-->
                        <div class="u-col u-col-100" style="max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;">
                            <div style="background-color: #ffffff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;">
                                <!--[if (!mso)&(!IE)]><!--><div style="box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;"><!--<![endif]-->

                                <table id="u_content_text_3" style="font-family:'Montserrat',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                                    <tbody>
                                    <tr>
                                        <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:10px 50px;font-family:'Montserrat',sans-serif;" align="left">

                                            <div style="font-family: 'Montserrat',sans-serif; font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;">
                                                <p style="line-height: 140%;">\${dashboardLink}</p>
                                            </div>

                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                                <table id="u_content_divider_6" style="font-family:'Montserrat',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                                    <tbody>
                                    <tr>
                                        <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:20px 50px 10px;font-family:'Montserrat',sans-serif;" align="left">

                                            <table height="0px" align="left" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 1px solid #ced4d9;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%">
                                                <tbody>
                                                <tr style="vertical-align: top">
                                                    <td style="word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%">
                                                        <span>&#160;</span>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>

                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                                <table id="u_content_text_deprecated_1" style="font-family:'Montserrat',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                                    <tbody>
                                    <tr>
                                        <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:10px 50px;font-family:'Montserrat',sans-serif;" align="left">

                                            <div style="font-family: 'Montserrat',sans-serif; line-height: 140%; text-align: left; word-wrap: break-word;">
                                                <p style="font-size: 14px; line-height: 140%; text-align: left;"><span style="font-family: Montserrat, sans-serif; line-height: 19.6px;">Please contact us at \${participantSupportEmailLink} if you have any questions.</span></p>
                                            </div>

                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                                <table id="u_content_text_4" style="font-family:'Montserrat',sans-serif;" role="presentation" cellpadding="0" cellspacing="0" width="100%" border="0">
                                    <tbody>
                                    <tr>
                                        <td class="v-container-padding-padding" style="overflow-wrap:break-word;word-break:break-word;padding:10px 50px 50px;font-family:'Montserrat',sans-serif;" align="left">

                                            <div style="font-family: 'Montserrat',sans-serif; font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;">
                                                <p style="line-height: 140%;">We are so grateful for your participation - thank you again!</p>
                                                <p style="line-height: 140%;">&nbsp;</p>
                                                <p style="line-height: 140%;">Best regards,</p>
                                                <p style="line-height: 140%;"><em>The Staff</em></p>
                                            </div>

                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                                <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->
                            </div>
                        </div>
                        <!--[if (mso)|(IE)]></td><![endif]-->
                        <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->
                    </div>
                </div>
            </div>


            <!--[if (mso)|(IE)]></td></tr></table><![endif]-->
        </td>
    </tr>
    </tbody>
</table>
<!--[if mso]></div><![endif]-->
<!--[if IE]></div><![endif]-->
</body>

</html>
`
