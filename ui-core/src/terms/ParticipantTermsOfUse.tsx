import classNames from 'classnames'
import React from 'react'
import { Link } from 'react-router-dom'

type SectionHeadingProps = JSX.IntrinsicElements['h2']

const SectionHeading = (props: SectionHeadingProps) => {
  const { className, ...otherProps } = props
  return <h2 {...otherProps} className={classNames('h3', className)} />
}

export const ParticipantTermsOfUse = () => {
  return (
    <>
      <h1 className="h2 text-center mb-5">
        Broad Institute
        <br />
        Juniper Terms of Use
      </h1>
      <p>
        THE PLATFORM IS NOT INTENDED FOR USE IN THE EVENT OF AN EMERGENCY OR
        OTHER URGENT SITUATION. IF YOU BELIEVE YOU MAY HAVE A MEDICAL EMERGENCY,
        CALL 911 OR YOUR LOCAL EMERGENCY MEDICAL SYSTEM IMMEDIATELY.
      </p>
      <p>Effective Date: April 28, 2023</p>

      <p>
        Please read these Terms of Use (these <strong>“Terms”</strong>)
        carefully, as they constitute a binding contract between you, an
        individual user (<strong>“you”</strong> or <strong>“your”</strong>), and
        The Broad Institute, Inc. (<strong>“Broad,”</strong>{' '}
        <strong>“we,”</strong> <strong>“us,”</strong> or <strong>“our”</strong>
        ).
      </p>
      <p>
        These Terms of Use (<strong>“Terms”</strong>) apply to your use of the
        Juniper Platform, which can be accessed by our website{' '}
        <a href={window.location.origin}>{window.location.hostname}</a>{' '}
        (collectively, the <strong>“Platform”</strong>). By clicking “I accept”
        or by using or accessing the Platform, you agree to be bound by these
        Terms. If you do not agree to these Terms, you may not access or use the
        Platform.
      </p>
      <p>
        We reserve the right to change or otherwise modify these Terms at any
        time. All changes are effective immediately when we post them, and apply
        to all access to and use of the Platform thereafter. We may also notify
        you by sending an email notification to the address associated with your
        Account (as defined below) or providing notice through our Platform.
        Your continued access or use of the Platform after receiving notice of
        any update, modification, or other change to these Terms signifies your
        acceptance thereof.
      </p>

      <SectionHeading>1. General Overview of the Platform</SectionHeading>
      <p>
        We have developed the Platform to enable users participating in certain
        clinical research projects (each, a <strong>“Research Project”</strong>)
        to input data to be analyzed by disease foundations, researchers, their
        staff, and their collaborators. The Platform provides functionality and
        resources to connect research participants with investigators who
        conduct direct-to-participant studies for human subjects research. By
        participating in a Research Project, you may be subject to additional
        terms and conditions imposed by that Research Project’s sponsor or the
        institution at which the research is conducted. Those terms may be
        presented to you in the Platform or through alternate means. The
        Research Project’s sponsor may require you to complete an informed
        consent process before participating in a Research Project on the
        Platform.
      </p>

      <SectionHeading>
        2. Creating an Account; Accessing the Platform
      </SectionHeading>
      <p>
        To access and use certain features of the Platform, you might need to
        register for an account (<strong>“Account”</strong>) by creating a
        password and providing your name, email address, phone number, and other
        information. If you register for an Account, you must provide accurate
        information and promptly update this information if it changes. You may
        not permit any other person to access the Platform using your user name
        and password, and the use of your Account is your responsibility. If you
        learn or suspect that your user name or password has been wrongfully
        used or disclosed, you should promptly notify us and immediately reset
        your password. To help ensure the security of your password or Account,
        please sign out of your Account at the end of each session.
      </p>

      <SectionHeading>3. Privacy</SectionHeading>
      <p>
        Broad understands the importance of confidentiality and privacy
        regarding your information. Please see our{' '}
        <Link to="/privacy">Privacy Policy</Link> for a description of how we
        collect, use, and disclose your personal information when you access or
        use the Platform. By using the Platform or by clicking to accept or
        agree to these Terms when this option is made available to you, you
        consent to our use of your information and our contacting you, in each
        case, in compliance with our Privacy Policy.
      </p>

      <SectionHeading>4. Communication Preferences</SectionHeading>
      <p>
        By creating an Account, you also consent to receive electronic
        communications from Broad (e.g., via email, text message, or by posting
        notices to the Platform). These communications may include operational
        notices about your Account (e.g., password changes and other
        transactional information) and are part of your relationship with us.
        You agree that any communications that we send to you electronically
        will satisfy any legal communication requirements, including that such
        communications be in writing. When permitted by law, we may also send
        you promotional communications via email, including newsletters, special
        offers, surveys and other news and information we think will be of
        interest to you. You may opt out of receiving these promotional emails
        at any time by following the unsubscribe instructions provided.
      </p>

      <SectionHeading>
        5. Are There Any Requirements To Be Able To Use The Platform?
      </SectionHeading>
      <p>
        Yes, you must be at least 13 years of age in order to use the Platform.
        If you are over 13, but are under the age of majority in your
        jurisdiction, you represent and warrant that your legal guardian has
        reviewed and agrees to these Terms. Prospective participants in a
        Research Project can access certain features of the Platform, such as
        exploring aggregate data and viewing video content and posted articles,
        without first registering. However, in order to have full access to all
        features of the Platform, you must first create an Account, and may need
        to execute a consent form with one of the Research Projects to upload
        any information. You agree to provide complete and accurate information
        when registering to use the Platform and to keep that information
        updated; this ensures that we can properly identify and contact you.
      </p>
      <p>
        As a user of the Platform, you represent to us that you meet these
        eligibility requirements and that you have the right to provide the
        information and User Content (as defined in Section 7 below), and that
        such information and User Content does not violate these Terms or any
        other person’s rights or any law.
      </p>

      <SectionHeading>6. Proprietary Rights</SectionHeading>
      <p>
        Broad grants you a limited right to use the Platform for your personal
        use. All original content, materials, features, and functionality
        (including text, information, images, photos, graphics, artworks, logos,
        videos, audios, directories, listings, databases, and search engines)
        available via the Platform (the “Content”) are owned by Broad and/or its
        licensors and may be protected by U.S. and foreign copyright, trademark,
        and other intellectual property laws. Subject to your compliance with
        these Terms, we grant you a limited, non-exclusive, non-transferable
        right and license to access and use the Platform and Content solely for
        your personal, non-commercial use; provided, however, that such license
        does not include any right to (a) sell or resell our Platform and the
        Content; (b) copy, reproduce, distribute, publicly perform or publicly
        display Content, except as expressly permitted by us or our licensors;
        (c) modify the Content, remove any proprietary rights notices or
        markings, or otherwise make any derivative uses of our Platform and the
        Content; (d) use any data mining, medical robots or similar data
        gathering or extraction methods; and (e) use our Platform and the
        Content other than for their intended purposes. Except for this limited
        license granted to you, we reserve all other rights. This license may be
        revoked and terminated by us at any time and for any reason. Any
        unauthorized use, reproduction, or distribution of the Platform or
        Content is strictly prohibited and may result in termination of the
        license granted herein, as well as civil and/or criminal penalties.
      </p>
      <p>
        All trademarks, trade names and logos appearing on or through the
        Platform are owned or licensed by us . The “Broad Institute” name and
        logo and all other Broad names, marks, logos and other identifiers are
        trademarks and service marks of Broad. You may not use or display any
        Broad trademarks, trade names, or logos without our prior written
        permission. We reserve all rights.
      </p>
      <p>
        If you choose to provide us with any comments, suggestions, ideas or
        other feedback, you agree that we have an unrestricted right to use
        them, and you are not entitled to receive any compensation.
      </p>

      <SectionHeading>7. Uploaded Information And User Content</SectionHeading>
      <p>
        The Platform may contain medical and other information about
        participants in the Research Projects (
        <strong>“Participant Information”</strong>). By submitting any
        Participant Information, you hereby grant us the right to upload,
        reproduce, display, perform, transmit and distribute such Participant
        Information, including to Research Project investigators on the
        Platform. This enables the collaboration that is the goal of the
        Platform. As a user, you should only use the Participant Information in
        connection with the Platform, and in connection with the applicable
        Research Project in which you are participating.
      </p>
      <p>
        The Platform may contain features that allow you to post or provide
        information, comments and other content; we refer to this as “User
        Content”. You retain the right to your User Content. However, you grant
        to Broad the perpetual, irrevocable, fully transferable and royalty-free
        right and license to use the User Content for any purposes, including
        without limitation, to reproduce, distribute, publish, modify, display,
        perform, make derivative works, and for any and all commercial purposes,
        and in any and all media and formats, whether now known or hereafter
        created. This means we can use the User Content you submit in order to
        provide the Platform and use information you submit for the Research
        Projects.
      </p>

      <SectionHeading>8. Prohibited Uses</SectionHeading>
      <p>
        We want to make sure the Platform is a safe place for us and our users.
        For this reason, we need to have certain rules about the use of the
        Platform, including certain conduct that is prohibited. You agree not to
        use the Platform in any way, provide User Content or engage in any
        conduct that:
      </p>
      <ul>
        <li>is unlawful, illegal, or unauthorized;</li>
        <li>is defamatory of any other person;</li>
        <li>is obscene, sexually explicit, or offensive;</li>
        <li>advertises or promotes any other product or business;</li>
        <li>
          is likely to harass, upset, embarrass, alarm, or annoy any other
          person;
        </li>
        <li>is likely to disrupt our service in any way; or</li>
        <li>
          promotes discrimination based on race, sex, religion, nationality,
          disability, sexual orientation, or age;
        </li>
        <li>
          infringes any copyright, trademark, trade secret, or other proprietary
          right of any other person; or
        </li>
        <li>
          advocates, promotes or assists any violence or any unlawful act.
        </li>
      </ul>
      <p>
        We reserve the right, but do not have the obligation, at our sole
        discretion to edit, delete, remove or block any User Content that
        violates these Terms. In addition, we reserve the right at our sole
        discretion to terminate any user’s access to Platform if they violate
        this Section 8 or any other provision of these Terms.
      </p>

      <SectionHeading>
        9. Broad Is Not Responsible For Third-Party Content
      </SectionHeading>
      <p>
        The Platform may contain links to third-party web sites, products, and
        services, and it may redirect you to third party applications on your
        mobile device (each, a “Linked Third-Party Service”). Broad is not
        responsible for the content of Linked Third-Party Services, and does not
        make any representations or warranties regarding the content or accuracy
        of any such content. When you access and use a Linked Third-Party
        Service, you are subject to that third party’s terms and conditions of
        use and privacy policy and you agree that Broad is not responsible for
        and has made no representations or warranties, express or implied,
        regarding any Linked Third-Party Service and that Broad shall have no
        liability relating to such Linked Third-Party Service except to the
        extent such liability is caused by the negligence of Broad. Your use of
        any Linked Third-Party Service is at your own risk and subject to the
        terms and conditions of use for such offerings.
      </p>

      <SectionHeading>10. Disclaimer Of Warranties</SectionHeading>
      <p>
        We provide the Platform on an ‘as is’ and ‘as available’ basis without
        any promises or representations, expressed or implied. For example, we
        make no promises about how the Platform will work, that they will be
        error-free, uninterrupted or have no defects, or that the information
        you provide will result in any particular treatment, cure or other
        results in any Research Project. The law also provides certain implied
        warranties, such as warranties of merchantability and fitness for a
        particular use; we disclaim those warranties, meaning they do not apply
        to the Platform. Notwithstanding the foregoing, nothing in this
        paragraph is intended to free Broad from liability for its own
        negligence.
      </p>

      <SectionHeading>11. Limitation Of Liability</SectionHeading>
      <p>THIS SECTION LIMITS THE DAMAGE YOU CAN RECOVER FROM US. </p>
      <p>
        NOTWITHSTANDING THE FAILURE OF ESSENTIAL PURPOSE OF ANY LIMITED REMEDY
        OF ANY KIND, TO THE MAXIMUM EXTENT PERMITTED BY LAW, WE ARE NOT
        RESPONSIBLE OR LIABLE FOR ANY INDIRECT, INCIDENTAL, CONSEQUENTIAL,
        SPECIAL, EXEMPLARY, PUNITIVE, OR OTHER DAMAGES (INCLUDING WITHOUT
        LIMITATION ANY LOSS OF PROFITS, LOST SAVINGS, OR LOSS OF DATA) OR
        LIABILITIES UNDER ANY CONTRACT, STRICT LIABILITY, OR OTHER THEORY
        ARISING OUT OF OR RELATING IN ANY MANNER TO THE PLATFORM, WHETHER OR NOT
        WE HAVE BEEN INFORMED OF THE POSSIBILITY OF SUCH DAMAGES OR LIABILITIES.
        YOUR SOLE REMEDY WITH RESPECT TO THE PLATFORM IS TO STOP USING THE
        PLATFORM. NOTWITHSTANDING THE FOREGOING, NOTHING IN THIS PARAGRAPH IS
        INTENDED TO FREE BROAD FROM LIABILITY FOR ITS OWN NEGLIGENCE.
      </p>
      <p>
        SOME STATES DO NOT ALLOW THE EXCLUSION OR LIMITATION OF CERTAIN
        WARRANTIES AND/OR LIABILITIES, SO CERTAIN OF THE ABOVE LIMITATIONS OR
        EXCLUSIONS MAY NOT APPLY TO YOU.
      </p>
      <p>
        In the event that applicable law does not allow the disclaimer of
        certain warranties and/or the limitation of liability for direct,
        indirect, consequential or other damages, in no event shall Indemnitees’
        liability arising under or in connection with these Terms and your use
        of the Platform exceed $100, except to the extent such damages result
        from Broad’s negligence.
      </p>

      <SectionHeading>12. Termination</SectionHeading>
      <p>
        These Terms are effective unless and until terminated by either you or
        Broad. You may terminate these Terms at any time, provided that you
        discontinue any further use of the Platform. We also may terminate or
        suspend these Terms, at any time, without notice and accordingly deny
        you access to the Platform, for any reason, including without
        limitation, if at our sole discretion you fail to comply with any
        provision of these Terms or your use is harmful to the interests of
        another user of the Platform. Upon any termination of the Terms by
        either you or us, you must promptly cease using the Platform.
      </p>
      <p>
        Termination will not limit any of our other rights and remedies. Any
        provision that must survive in order to give proper effect to the intent
        and purpose of these Terms shall survive termination.
      </p>

      <SectionHeading>13. General</SectionHeading>
      <p>
        These Terms, including the Privacy Policy and other policies
        incorporated herein, constitute the entire and only agreement between
        you and Broad with respect to the subject matter of these Terms, and
        supersede any and all prior or contemporaneous agreements,
        representations, warranties and understandings, written or oral, with
        respect to the subject matter of these Terms. If any provision of these
        Terms is found to be unlawful, void or for any reason unenforceable,
        then that provision shall be deemed severable from these Terms and shall
        not affect the validity and enforceability of any remaining provisions.
        Neither these Terms nor any right, obligation or remedy hereunder is
        assignable, transferable, delegable or sub-licensable by you except with
        Broad’s prior written consent, and any attempted assignment, transfer,
        delegation or sublicense shall be null and void. Broad may assign,
        transfer or delegate this or any right or obligation or remedy hereunder
        in its sole discretion. No waiver by either party of any breach or
        default hereunder shall be deemed to be a waiver of any preceding or
        subsequent breach or default. Any heading, caption, or section title
        contained in these Terms is inserted only as a matter of convenience and
        in no way defines or explains any section or provision hereof.
      </p>

      <SectionHeading>14. Contact Us</SectionHeading>
      <p>
        If you have any questions regarding our Platform or these Terms, you can
        contact us at{' '}
        <a href="mailto:support@juniper.terra.bio">support@juniper.terra.bio</a>
      </p>
    </>
  )
}
