package com.phonebid.app.chat.domain;

import com.phonebid.app.auction.domain.Quote;
import com.phonebid.app.common.domain.BaseEntity;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Comment("채팅방 고유 ID (UUID)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false, unique = true)
    @Comment("연결된 견적 ID")
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    @Comment("구매자 ID")
    private User consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    @Comment("연결된 판매자 ID")
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("채팅방 상태 (ACTIVE, CLOSED)")
    private ChatRoomStatus status;

    @Builder
    public ChatRoom(Quote quote, User consumer, Seller seller) {
        this.quote = quote;
        this.consumer = consumer;
        this.seller = seller;
        this.status = ChatRoomStatus.ACTIVE;
    }

    public void close() {
        if (this.status == ChatRoomStatus.CLOSED) {
            return;
        }
        this.status = ChatRoomStatus.CLOSED;
    }

    public void reopen() {
        if (this.status == ChatRoomStatus.ACTIVE) {
            return;
        }
        this.status = ChatRoomStatus.ACTIVE;
    }
}


