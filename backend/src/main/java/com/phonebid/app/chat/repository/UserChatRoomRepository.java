package com.phonebid.app.chat.repository;

import com.phonebid.app.chat.domain.ChatRoom;
import com.phonebid.app.chat.domain.UserChatRoom;
import com.phonebid.app.member.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, UUID> {

    /**
     * 사용자와 채팅방으로 UserChatRoom 조회 (나간 채팅방 포함)
     */
    Optional<UserChatRoom> findByUserAndChatRoom(User user, ChatRoom chatRoom);

    /**
     * 사용자 ID와 채팅방 ID로 활성 상태의 UserChatRoom 조회 (나가지 않은 채팅방)
     */
    @Query("SELECT ucr FROM UserChatRoom ucr " +
           "WHERE ucr.user.id = :userId AND ucr.chatRoom.id = :chatRoomId AND ucr.deletedAt IS NULL")
    Optional<UserChatRoom> findActiveByUserIdAndChatRoomId(@Param("userId") UUID userId, 
                                                             @Param("chatRoomId") UUID chatRoomId);

    /**
     * 사용자가 참여한 활성 채팅방 목록 조회 (페이징)
     * deletedAt이 null인 것만 조회
     */
    @Query("SELECT ucr FROM UserChatRoom ucr " +
           "WHERE ucr.user.id = :userId AND ucr.deletedAt IS NULL " +
           "ORDER BY ucr.chatRoom.createdAt DESC")
    Page<UserChatRoom> findActiveChatRoomsByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * 채팅방에 참여한 모든 활성 사용자 조회
     */
    @Query("SELECT ucr FROM UserChatRoom ucr " +
           "WHERE ucr.chatRoom.id = :chatRoomId AND ucr.deletedAt IS NULL")
    List<UserChatRoom> findActiveUsersByChatRoomId(@Param("chatRoomId") UUID chatRoomId);

    /**
     * 채팅방에 특정 사용자가 참여 중인지 확인
     */
    @Query("SELECT COUNT(ucr) > 0 FROM UserChatRoom ucr " +
           "WHERE ucr.user.id = :userId AND ucr.chatRoom.id = :chatRoomId AND ucr.deletedAt IS NULL")
    boolean existsActiveByUserIdAndChatRoomId(@Param("userId") UUID userId, 
                                               @Param("chatRoomId") UUID chatRoomId);

    /**
     * 채팅방의 활성 멤버 수 조회 (deletedAt이 null인 사용자 수)
     */
    @Query("SELECT COUNT(ucr) FROM UserChatRoom ucr " +
           "WHERE ucr.chatRoom.id = :chatRoomId AND ucr.deletedAt IS NULL")
    long countActiveMembersByChatRoomId(@Param("chatRoomId") UUID chatRoomId);

    /**
     * 채팅방의 모든 UserChatRoom 삭제 (hard delete)
     */
    @Modifying
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") UUID chatRoomId);
}

