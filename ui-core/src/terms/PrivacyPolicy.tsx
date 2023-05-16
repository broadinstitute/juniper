import classNames from 'classnames'
import React from 'react'
import { Link } from 'react-router-dom'

type SectionHeadingProps = JSX.IntrinsicElements['h2']

const SectionHeading = (props: SectionHeadingProps) => {
  const { className, ...otherProps } = props
  return <h2 {...otherProps} className={classNames('h3', className)} />
}

export const PrivacyPolicy = () => {
  return (
    <>
      <h1 className="h2 text-center mb-5">
        The Broad Institute, Inc.
        <br />
        Juniper Platform
        <br />
        Privacy Policy
      </h1>
      <p>Last Updated: April 28, 2023</p>

      <p>
        The Broad Institute, Inc. (<strong>“Broad,”</strong>{' '}
        <strong>“we,”</strong> <strong>“us,”</strong> or <strong>“our”</strong>)
        values your privacy. This Privacy Policy (<strong>“Policy”</strong>)
        provides important information about how Broad collects and uses your
        Personal Information when you access or use the Juniper Platform (the{' '}
        <strong>“Platform”</strong>). Please review this Policy, which is
        incorporated into and made part of the Terms of Use that apply to you:{' '}
        <Link to="/terms/participant">Participant Terms of Use</Link> or{' '}
        <Link to="/terms/investigator">Investigator Terms of Use</Link>. By
        accessing or using the Platform, you consent to our collection, use, and
        disclosure of your information in accordance with this Policy and our
        Terms of Use. If you do not agree to our Policy or Terms of Use, you may
        not use the Platform.
      </p>
      <p>
        If you are a participant in a research project (
        <strong>“Participant User”</strong>) supported by the Platform (each, a{' '}
        <strong>“Research Project”</strong>), the Research Project will collect
        health-related information from you regarding your medical and health
        conditions, treatments, and diagnosis (
        <strong>“Participant User Information”</strong>). Some Participant User
        Information may be gathered from you prior to the Research Project
        commencing to determine if you qualify to participate in the Research
        Project, and other information is collected after you have qualified to
        participate and, where applicable to the Research Project, have signed a
        written consent form to participate. Participant User Information may be
        uploaded to the Platform by you or by persons conducting the Research
        Project.
      </p>
      <p>
        This Privacy Policy also describes how Broad collects, uses, and
        discloses personal information of users who are investigators and or
        other researchers (each, an <strong>“Investigator User”</strong>).
      </p>
      <p>
        <strong>Important Note to Participant Users</strong>: By participating
        in a Research Project, you may be subject to additional policies,
        including a privacy policy from that Research Project’s sponsor or the
        institution at which the research is conducted. That privacy policy may
        be presented to you in the Platform or through alternate means. You may
        also need to complete an informed consent process before participating
        in research on the Platform. If you are participating in a Research
        Project, you may be asked to provide Participant User Information,
        blood, saliva, and other types of samples to assist in research and
        development activities. The Investigator Users will obtain your
        separate, written consent to use these samples, and our research
        partners’ use of these samples and your Participant User Information
        will be governed by the terms of the consent that you sign and not this
        Policy.
      </p>

      <SectionHeading>Information We Collect</SectionHeading>
      <p>
        We collect information (including Personal Information) about you in two
        ways. The first is information you voluntarily provide to us when you
        choose to do any of the following, none of which is required:
      </p>
      <ul>
        <li>
          Register to use the Platform, such as when you create an account.
        </li>
        <li>
          Use the Platform to provide us information about you, such as your
          experiences in connection with Research Projects in which you
          participate.
        </li>
        <li>Use the Platform as an Investigator User.</li>
        <li>Respond to surveys we or the researchers may send to you.</li>
        <li>Communicate with us (e.g., by email).</li>
      </ul>

      <p>
        The second way we collect information is through certain automated
        technologies that are used when you access our Platform. An example is a
        “cookie,” which is a small piece of computer code that is placed on your
        computer or other device that can be used for various purposes, such as
        to recognize a user or to track their activities on a website. We
        describe automated technologies in more detail below. We may collect
        various information through these automated technologies, including your
        Device Information as described below, along with how you use our
        Platform, such as the web pages you view, the links you click, the
        length of time you spend visiting our Platform, and the web page or web
        site that led you to our Platform.
      </p>

      <SectionHeading>What kind of information do we collect?</SectionHeading>
      <p>We collect various types of information:</p>
      <ul>
        <li>
          <strong>“Personal Information”</strong> is information that alone or
          in combination with other information may be used to readily identify,
          contact, or locate you. For example, your name, age, address, phone
          number, email address, clinical data, medical history, and treatment
          type are Personal Information we may collect through your use of the
          Platform once a user has consented to participate in a study.
        </li>
        <li>
          <strong>“Device Information”</strong> is information that relates to a
          particular computer, mobile device, or other device (e.g., iPad) that
          you use to access the Platform. Device Information includes such
          things as an IP address (which is a number assigned by an internet
          service provider to the computer you use to access the Internet), a
          device ID (which is a number assigned to a mobile phone by the device
          manufacturer), or the type of operating system or web browser used to
          access the Platform.
        </li>
        <li>
          <strong>Other information you choose to provide</strong>, such as when
          you participate in a survey or when you request technical or customer
          support.
        </li>
      </ul>

      <SectionHeading>
        Do we use cookies and other tracking technologies?
      </SectionHeading>
      <p>
        Yes. We use cookies for various purposes, such as to make it easier for
        you to navigate our Platform, to enable a faster log-in process, or to
        allow us to track your activities on our Platform. There are two types
        of cookies: session and persistent cookies.
      </p>

      <ul>
        <li>
          <strong className="text-decoration-underline">Session Cookies</strong>
          . Session Cookies exist only during a session. They disappear from
          your computer or device when you close your browser or turn off your
          computer or device. We use Session Cookies to allow our systems to
          uniquely identify you during a session or while you are logged in to
          the Platform. This allows us to process your communications and
          requests and verify your identity after you have logged in and as you
          move through our Platform, and to optimize your experience in using
          the Platform.
        </li>
        <li>
          <strong className="text-decoration-underline">
            Persistent Cookies
          </strong>
          . Persistent Cookies remain on your computer or device after you have
          closed your browser or turned off your computer or mobile device. We
          use Persistent Cookies to track aggregate and statistical information
          about user activity on our Platform (see{' '}
          <strong>“Tracking Technologies”</strong> below).
        </li>
      </ul>
      <p>
        <strong>Disabling Cookies</strong>. Most web browsers automatically
        accept cookies, but if you prefer, you can edit your browser options to
        block them in the future. The Help portion of the toolbar on most
        browsers will tell you how to prevent your computer from accepting new
        cookies, how to have the browser notify you when you receive a new
        cookie, or how to disable cookies altogether. Visitors to our Platform
        who disable cookies may not be able to use the Platform.
      </p>
      <p>
        <strong>Tracking Technologies</strong>. We use analytics tools to
        evaluate our Platform. We use these tools to help us improve our
        Platform, performance, and user experiences. We may also use third-party
        tracking technologies to detect malicious code or attacks on our
        Platform. These entities may use cookies and other tracking technologies
        to perform their services.
      </p>
      <SectionHeading>Do you respond to Do Not Track signals?</SectionHeading>
      <p>
        Do Not Track (<strong>“DNT”</strong>) is a privacy preference that users
        can set in their web browsers. When users turn on DNT, their browser
        sends a message to websites requesting that they do not track the user.
        However, our Platform does not change its information collection in
        response to DNT browser settings or signals. For information about DNT,
        visit <a href="https://www.allaboutdnt.org">www.allaboutdnt.org</a>.
      </p>

      <SectionHeading>How do we use your information?</SectionHeading>
      <p>
        We use your information, including your Personal Information, for
        various purposes:
      </p>
      <ul>
        <li>
          <strong className="text-decoration-underline">
            Conduct Research
          </strong>
          . We, and the research partners to whom we provide the Platform and
          also those with whom we collaborate, use the information you provide
          about yourself, your medical condition, and experiences to conduct
          research, but only if you are a Participant User that has enrolled in
          a Research Project in which case you may need to execute a separate
          written consent with the applicable Research Project.
        </li>
        <li>
          <strong className="text-decoration-underline">
            Provide the Platform
          </strong>
          . We, and our vendors and service providers, use your information
          (e.g., your profile information, user identification, and password) to
          provide the Platform, respond to your inquiries, and troubleshoot
          issues with the Platform, and for other customer service purposes.
        </li>
        <li>
          <strong className="text-decoration-underline">
            Personalize Your Experience
          </strong>
          . We use your information to tailor the content and information that
          we may send or display to you, to offer location customization, and
          personalized help and instructions, and to otherwise personalize your
          experiences while you are using the Platform.
        </li>
        <li>
          <strong className="text-decoration-underline">
            Improve and Develop Our Platform
          </strong>
          . We use your information to ensure our Platform is working as
          intended, to better understand how users access and use our Platform,
          both on an aggregated and individualized basis, to make improvements
          to our Platform, to develop new features and applications, and for
          other research and analytical purposes.
        </li>
      </ul>
      <p>
        We may share aggregate data from our cohorts to encourage participation
        in our Research Projects. This aggregate data does not identify you
        personally.
      </p>

      <SectionHeading>With whom do we share your information?</SectionHeading>
      <p>
        We may need to share your information, including your Personal
        Information, with third parties. The following are the categories of
        third parties with whom we may share your Personal Information:
      </p>
      <ul>
        <li>
          <strong className="text-decoration-underline">
            Research Partners
          </strong>
          . The purpose of the Platform is to facilitate the collection and
          analysis of information for research purposes. For example, the
          information you provide through the Platform may be used by various
          persons involved in a Research Project such as the principal
          investigator and other clinicians and Investigator Users involved in
          conducting research in a particular Research Project. If you are asked
          to sign an informed consent to participate in a Research Project, we
          may also share your information with the third parties listed on the
          consent that you sign and for the purposes listed on the consent.
        </li>
        <li>
          <strong className="text-decoration-underline">
            Vendors and Service Providers
          </strong>
          . We use various third-party vendors and service providers that assist
          us in providing the Platform. For example, we may use third-party
          vendors to store and authenticate account credentials, send email
          communications, ship test kits, and for hosting and storing
          information collected through our Platform. We may share your
          information with these vendors and service providers to enable them to
          provide these services to us. These service providers and vendors are
          required to only use your information to provide their services to us
          and in a manner consistent with this Policy.
        </li>
        <li>
          <strong className="text-decoration-underline">
            As Required By Law
          </strong>
          . We may be required, by law, to disclose your Personal Information to
          third parties, such as in the following situations:
          <ul>
            <li>In response to a request by law enforcement;</li>
            <li>
              In response to legal process, such as a subpoena, a request for
              discovery in a civil proceeding, or in response to a court order.
            </li>
          </ul>
        </li>
        <li>
          <strong className="text-decoration-underline">
            Enforce Our Terms and Protect You, Us, and Others
          </strong>
          . We may access, preserve, and disclose your Personal Information to
          enforce the Terms of Use for the Platform and protect your, our, or
          others’ rights, property, or safety.
        </li>
        <li>
          <strong className="text-decoration-underline">
            Merger, Sale, or Other Corporate Transactions
          </strong>
          . A legal entity, such as Broad, may be involved in a business
          transaction where its ownership or assets are sold or transferred to
          another legal entity. This can happen in a merger with another legal
          entity, an acquisition by another legal entity, a corporate
          reorganization, or a legal proceeding (e.g., bankruptcy or
          receivership) where a trustee or other takes over control of the
          entity. In each of these situations, your information, including your
          Personal Information, may be sold or transferred as part of such a
          transaction as permitted by law and/or contract.
        </li>
        <li>
          <strong className="text-decoration-underline">
            With Your Consent
          </strong>
          . We may ask you from time to time to give us permission to share your
          information with other third parties not described in this section. In
          each case, we will describe that third party and the purpose for
          sharing your information.
        </li>
        <li>
          <strong className="text-decoration-underline">
            Aggregate and De-Identified Information
          </strong>
          . We may share aggregate or de-identified information about users and
          their use of the Platform with third parties and publicly for
          marketing, advertising, research, or similar purposes. This
          information will not identify you personally.
        </li>
      </ul>

      <SectionHeading>
        What rights do I have with respect to my information?
      </SectionHeading>
      <p>
        You can update your account at any time during the active period of the
        Research Study.
      </p>
      <p>
        We will respond to any user request as soon as we reasonably can, and
        within the time and in the manner required by law. We may request
        additional information from you to verify the request. We may not be
        able to accommodate all requests; for example, we may be unable to
        accommodate a deletion request if we are required to maintain
        information under law or a legal obligation or if information is used as
        part of a study.
      </p>

      <SectionHeading>
        What kind of security does the Platform use to protect my information?
      </SectionHeading>
      <p>
        We seek to use reasonable physical, technical, and administrative
        measures designed to protect personal information within our
        organization. Unfortunately, no data transmission, processing, or
        storage system can be guaranteed to be 100% secure. If you have reason
        to believe that your use of the Platform is no longer secure (for
        example, if you feel that the security of your account has been
        compromised), please immediately notify us in accordance with the
        “Contacting Us” section below.
      </p>

      <SectionHeading>Do you collect information from children?</SectionHeading>
      <p>
        Minors can join and participate in a Research Project with the consent
        of their parent or legal guardian as required by applicable law. If you
        are a parent or legal guardian providing personal information regarding
        your child, you represent and warrant that you have the right and
        authority to provide us such information.
      </p>

      <SectionHeading>
        Is there any transfer of Personal Information from one country to
        another?
      </SectionHeading>
      <p>
        We store the information you provide on servers in the United States. If
        you are a user of the Platform, you agree to the transfer of your
        Personal Information to the United States and acknowledge that the laws
        in the United States may offer less protection to your Personal
        Information than the laws where you live.
      </p>

      <SectionHeading>Links to Third-Party Websites</SectionHeading>
      <p>
        Our Platform may reference or provide links to third-party websites.
        Other websites may also reference or link to our Platform. Because these
        websites are not controlled by Broad, we are not responsible for the
        third-party websites. We encourage our users to be aware when they leave
        our Platform to review the privacy policies posted on each and every
        website that collects personally identifiable information. Please be
        aware that Broad does not control, endorse, screen or approve, nor are
        we responsible for, the privacy policies or information practices of
        third parties or their websites or mobile applications. Visiting these
        other websites is at your own risk.
      </p>

      <SectionHeading>
        Whom do I contact if I have a question or a complaint?
      </SectionHeading>
      <p>
        You may contact us at the address below if you have any questions or
        complaints about information practices or this Policy:
      </p>
      <p>
        By email to:{' '}
        <a href="mailto:support@juniper.terra.bio">support@juniper.terra.bio</a>
      </p>

      <SectionHeading>Will you update or change this Policy?</SectionHeading>
      <p>
        Yes, we may need to update or change this Policy for various reasons,
        such as to comply with changes in the law or to cover new features,
        services, or Research Projects provided through the Platform. If we
        update or change this Policy we will:
      </p>
      <ul>
        <li>Post the changes to the Policy on the Platform; and</li>
        <li>
          Endeavor to notify you of any material changes to the Policy through
          the email address in your profile.
        </li>
      </ul>
      <p>
        To make sure you are aware of any updates or changes, you should review
        this Policy periodically and make sure you have your most current email
        address in your account. We will post the effective date of any new
        policy.
      </p>
    </>
  )
}
