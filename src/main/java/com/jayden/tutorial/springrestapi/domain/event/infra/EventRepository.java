package com.jayden.tutorial.springrestapi.domain.event.infra;

import com.jayden.tutorial.springrestapi.domain.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
