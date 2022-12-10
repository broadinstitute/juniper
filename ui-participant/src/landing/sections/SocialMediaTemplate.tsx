import _ from 'lodash'
import React from 'react'
import { ButtonConfig } from 'api/api'
/* eslint-disable */
type SocialMediaTemplateProps = {
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  facebookHref?: string, // URL of Facebook page
  instagramHref?: string, // URL of Instagram page
  twitterHref?: string, // URL of Twitter page
}

/**
 * Template for a hero with social media links.
 * TODO -- implement images
 */
function SocialMediaTemplate({
  config: {
    blurb,
    buttons,
    facebookHref,
    instagramHref,
    title,
    twitterHref
  }
}: {config: SocialMediaTemplateProps}) {
  return <div className="container py-5">
    <div className="d-flex justify-content-center mt-5 mb-4">
      {/** twitterHref &&
        <ArborImage imageStableId={'ourHealthContent_1_twitter.png'} alt={'Twitter'}
                      className="m-3" style={{width: '56px'}}/>*/
      }
      {/** facebookHref &&
          <ArborImage imageStableId={'ourHealthContent_1_facebook.png'} alt={'Facebook'}
                      className="m-3" style={{width: '54px'}}/> */
      }
      {/** instagramHref &&
          <ArborImage imageStableId={'ourHealthContent_1_instagram.png'} alt={'Instagram'}
                      className="m-3" style={{width: '49px'}}/> */
      }
    </div>
    <h1 className="fs-1 fw-normal lh-sm text-center">
      {title}
    </h1>
    <p className="fs-5 fw-normal text-center">
      {blurb}
    </p>
    <div className="d-grid gap-2 d-sm-flex justify-content-sm-center">
      {
        _.map(buttons, ({ text, href }) => {
          return <a href={href} role={'button'} className="btn btn-primary btn-lg px-4 me-md-2">{text}</a>
        })
      }
    </div>
  </div>
}

export default SocialMediaTemplate
