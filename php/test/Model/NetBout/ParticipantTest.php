<?php
/**
 * @version $Id$
 */

require_once 'FaZend/Test/TestCase.php';

class Model_NetBout_ParticipantTest extends FaZend_Test_TestCase
{
    public function testValidatesExistenceOfClassUnderTest()
    {
        $this->assertTrue(class_exists(substr(get_class(), 0, -4)));
    }

    public function testSendsInvitationToNetboutParticipant()
    {
        $inviter = Mocks_Model_User::get(null, 'testuser1');
        $invited = Mocks_Model_User::get('invited@email.com');
        $netBout = Mocks_Model_NetBout::get();

        $participant = Model_NetBout_Participant::create($netBout, $invited, $inviter);

        // @todo remove this line when this test will be workable
        $this->markTestIncomplete();

        $participant->sendInvitation();
    }

    public function testAcceptsInvitation()
    {
        $this->markTestIncomplete();
    }

    public function testDeclinesInvitation()
    {
        $this->markTestIncomplete();
    }

    /**
     * When user accept invitation, it should be able to participate in the
     * NetBout for which he was invited
     *
     * @todo #16:2h Model_Acl should be implemented with persistence storage to make this
     * scenario possible
     */
    public function testValidatesParticipantAccessToAcceptedInvitation()
    {
        $this->markTestIncomplete();

        $inviter = Mocks_Model_User::get(null, 'testuser1');
        $netBout = Mocks_Model_NetBout::get($inviter);
        $this->assertTrue(Model_Acl::isAllowed($inviter, $netBout));

        $invited = Mocks_Model_User::get('invited@email.com');

        $this->assertFalse(Model_Acl::isAllowed($user, $netBout, Model_Acl::PRIVILEGE_PARTICIPATE));
        $participant = Model_NetBout_Participant::create($netBout, $invited, $inviter);
        $participant->acceptInvitation();
        $this->assertTrue(Model_Acl::isAllowed($user, $netBout, Model_Acl::PRIVILEGE_PARTICIPATE));
    }

    /**
     * Expired invitations
     *
     * Not accepted invitation should be automatically declined after some
     * configured interval of time (7 days)
     */
    public function testValidatesInvitationAutomaticExpiration()
    {
       $this->markTestIncomplete();
    }
}