/*
 * This file contains template objects for each section type. These are used to populate
 * the raw JSON section editor in the HtmlSectionEditor component. These templates aren't
 * exhaustive; they do not provide every possible field for each section type, but they
 * are a good starting point for creating new sections. Eventually we'll build a UI on
 * top of this to make fields more configurable and easier to use.
 */

const templateImageConfig = {
  cleanFileName: 'Enter cleanFileName here',
  version: 1,
  alt: 'Enter alt text here',
  style: {
    height: '40px'
  }
}

const templatePhotoBio = {
  image: { ...templateImageConfig },
  name: 'Enter name here',
  title: 'Enter title here',
  blurb: 'Enter blurb here'
}

const templateSubgrid = {
  title: 'Enter title here',
  photoBios: [{
    ...templatePhotoBio
  }]
}

const templateMailingListButtonConfig = {
  text: 'Join Mailing List',
  type: 'mailingList'
}

const templateItemSection = {
  title: 'Enter title here',
  items: [{
    ...templateMailingListButtonConfig
  }]
}

const templateQuestion = {
  question: 'Enter question here',
  answer: 'Enter answer here'
}

export const sectionTemplates: Record<string, object> = {
  'FAQ': {
    title: 'Frequently Asked Questions',
    blurb: 'Enter blurb here',
    questions: [
      {
        ...templateQuestion
      },
      {
        ...templateQuestion
      }
    ],
    showToggleAllButton: true
  },
  'HERO_CENTERED': {
    title: 'Enter title here',
    blurb: 'Enter blurb here',
    blurbAlign: 'center',
    buttons: [
      {
        ...templateMailingListButtonConfig
      }
    ]
  },
  'HERO_WITH_IMAGE': {
    title: 'Enter title here',
    blurb: 'Enter blurb here',
    fullWidth: true,
    image: {
      ...templateImageConfig
    },
    imagePosition: 'right',
    buttons: [
      {
        ...templateMailingListButtonConfig
      }
    ],
    logos: [
      {
        ...templateImageConfig
      }
    ]
  },
  'SOCIAL_MEDIA': {
    twitterHandle: 'Enter Twitter handle here',
    instagramHandle: 'Enter Instagram handle here',
    facebookHandle: 'Enter Facebook handle here',
    paddingBottom: 0
  },
  'STEP_OVERVIEW': {
    title: 'Enter title here',
    steps: [
      {
        blurb: 'Enter blurb here',
        duration: '15-45 minutes',
        image: {
          ...templateImageConfig
        }
      }
    ]
  },
  'PHOTO_BLURB_GRID': {
    title: 'Enter title here',
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
    blurb: 'Enter blurb here',
    image: {
      ...templateImageConfig
    },
    imagePosition: 'right',
    stepNumberText: 'STEP 1',
    timeIndication: '45+ minutes',
    title: 'Enter title here'
  },
  'LINK_SECTIONS_FOOTER': {
    itemSections: [
      {
        ...templateItemSection
      }
    ]
  },
  'BANNER_IMAGE': {
    image: {
      ...templateImageConfig
    }
  }
}
