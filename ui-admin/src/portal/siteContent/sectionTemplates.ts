/*
 * This file contains template objects for each section type. These are used to populate
 * the raw JSON section editor in the HtmlSectionEditor component. These templates aren't
 * exhaustive; they do not provide every possible field for each section type, but they
 * are a good starting point for creating new sections. Eventually we'll build a UI on
 * top of this to make fields more configurable and easier to use.
 */

const templateImageConfig = {
  cleanFileName: '',
  version: 1,
  alt: '',
  style: {
    height: '40px'
  }
}

const templatePhotoBio = {
  image: { ...templateImageConfig },
  name: '',
  title: '',
  blurb: ''
}

const templateSubgrid = {
  title: '',
  photoBios: [{
    ...templatePhotoBio
  }]
}

const templateMailingListButtonConfig = {
  text: 'Join Mailing List',
  type: 'mailingList'
}

const templateLinkConfig = {
  text: '',
  href: ''
}

const templateItemSection = {
  title: '',
  items: [{
    ...templateLinkConfig
  },
  {
    ...templateLinkConfig
  }]
}

const templateQuestion = {
  question: '',
  answer: ''
}

export const sectionTemplates: Record<string, object> = {
  'FAQ': {
    title: 'Frequently Asked Questions',
    blurb: '',
    questions: [],
    showToggleAllButton: true
  },
  'HERO_CENTERED': {
    title: '',
    blurb: '',
    blurbAlign: 'center',
    buttons: []
  },
  'HERO_WITH_IMAGE': {
    title: '',
    blurb: '',
    fullWidth: true,
    image: {
      ...templateImageConfig
    },
    imagePosition: 'right',
    buttons: [],
    logos: []
  },
  'SOCIAL_MEDIA': {
    xHandle: '',
    instagramHandle: '',
    facebookHandle: '',
    tiktokHandle: '',
    linkedinHandle: '',
    threadsHandle: '',
    youtubeHandle: '',
    paddingBottom: 0
  },
  'STEP_OVERVIEW': {
    title: '',
    showStepNumbers: true,
    steps: [],
    buttons: []
  },
  'PHOTO_BLURB_GRID': {
    title: '',
    subGrids: [
      {
        ...templateSubgrid
      }
    ]
  },
  'PARTICIPATION_DETAIL': {
    actionButton: {
      ...templateMailingListButtonConfig
    },
    blurb: '',
    image: {
      ...templateImageConfig
    },
    imagePosition: 'right',
    stepNumberText: '',
    timeIndication: '',
    title: ''
  },
  'LINK_SECTIONS_FOOTER': {
    itemSections: [
      {
        ...templateItemSection
      },
      {
        ...templateItemSection
      },
      {
        ...templateItemSection
      }
    ]
  },
  'BANNER_IMAGE': {
    image: {
      cleanFileName: '',
      version: 1,
      alt: ''
    }
  }
}
