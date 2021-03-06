package org.motechproject.event.aggregation.service.impl;

import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.event.aggregation.aggregate.EventAggregator;
import org.motechproject.event.aggregation.model.AggregationState;
import org.motechproject.event.aggregation.model.event.PeriodicDispatchEvent;
import org.motechproject.event.aggregation.model.event.SporadicDispatchEvent;
import org.motechproject.event.aggregation.model.mapper.AggregationRuleMapper;
import org.motechproject.event.aggregation.repository.AllAggregatedEvents;
import org.motechproject.event.aggregation.repository.AllAggregationRules;
import org.motechproject.event.aggregation.service.EventAggregationService;
import org.motechproject.event.listener.EventListenerRegistryService;
import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.scheduler.domain.CronSchedulableJob;
import org.motechproject.scheduler.domain.RepeatingSchedulableJob;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.commons.date.util.DateUtil.newDateTime;
import static org.motechproject.testing.utils.TimeFaker.fakeNow;
import static org.motechproject.testing.utils.TimeFaker.stopFakingTime;

public class EventAggregationServiceImplTest {

    @Mock
    private AllAggregationRules allAggregationRules;
    @Mock
    EventListenerRegistryService eventListenerRegistryService;
    @Mock
    private AllAggregatedEvents allAggregatedEvents;
    @Mock
    private MotechSchedulerService schedulerService;

    EventAggregationService eventAggregationService;
    
    private final long MILLIS_IN_A_DAY = Period.days(1).toStandardSeconds().getSeconds() * 1000;
    private final long MILLIS_IN_A_MINUTE = 60 * 1000;
    private AggregationRuleMapper aggregationRuleMapper;

    @Before
    public void setup() {
        initMocks(this);
        eventAggregationService = new EventAggregationServiceImpl(allAggregationRules, eventListenerRegistryService, allAggregatedEvents, schedulerService);
        aggregationRuleMapper = new AggregationRuleMapper();
    }

    @Test
    public void shouldCreateAggregationRule() {
        eventAggregationService.createRule(new AggregationRuleRequest("foo", "", "event", new ArrayList<String>(), new PeriodicAggregationRequest("1 day", newDateTime(2012, 5, 22)), "aggregation", AggregationState.Running));

        verify(allAggregationRules).addOrReplace(aggregationRuleMapper.toRecord(new AggregationRuleRequest("foo", "", "event", new ArrayList<String>(), new PeriodicAggregationRequest("1 day", newDateTime(2012, 5, 22)), "aggregation", AggregationState.Running)));
    }

    @Test
    public void shouldSubscribeToEventThatsBeingAggregated() {
        eventAggregationService.createRule(new AggregationRuleRequest("foo", "", "event", asList("bar", "maz"), new PeriodicAggregationRequest("1 day", newDateTime(2012, 5, 22)), "aggregation", AggregationState.Running));

        verify(eventListenerRegistryService).registerListener(new EventAggregator("foo", asList("bar", "maz"), allAggregatedEvents, allAggregationRules), "event");
    }

    @Test
    public void shouldScheduleRepeatJobToDispatchPeriodicAggregation() {
        AggregationRuleRequest request = new AggregationRuleRequest("foo", "", "event", asList("bar", "maz"), new PeriodicAggregationRequest("1 day", newDateTime(2010, 10, 1)), "aggregation", AggregationState.Running);
        eventAggregationService.createRule(request);

        RepeatingSchedulableJob job = new RepeatingSchedulableJob(new PeriodicDispatchEvent(aggregationRuleMapper.toRecord(request)
).toMotechEvent(), newDateTime(2010, 10, 1).toDate(), null, MILLIS_IN_A_DAY, true);

        verify(schedulerService).safeScheduleRepeatingJob(job);
    }

    @Test
    public void shouldScheduleCronJobToDispatchCronBasedAggregation() {
        AggregationRuleRequest request = new AggregationRuleRequest("foo", "", "event", asList("bar", "maz"), new CronBasedAggregationRequest("* * * * *"), "aggregation", AggregationState.Running);
        eventAggregationService.createRule(request);

        CronSchedulableJob job = new CronSchedulableJob(new PeriodicDispatchEvent(aggregationRuleMapper.toRecord(request)
).toMotechEvent(), "* * * * *");

        verify(schedulerService).safeScheduleJob(job);
    }
    
    @Test
    public void shouldScheduleMinutelyJobToDispatchAggregationsInARollingWindow() {
        try {
            fakeNow(newDateTime(2010, 10, 1));

            AggregationRuleRequest request = new AggregationRuleRequest("foo", "", "event", asList("bar", "maz"), new CustomAggregationRequest("exp"), "aggregation", AggregationState.Running);
            eventAggregationService.createRule(request);

            RepeatingSchedulableJob job = new RepeatingSchedulableJob(new SporadicDispatchEvent(aggregationRuleMapper.toRecord(request)
, "exp").toMotechEvent(), newDateTime(2010, 10, 1).toDate(), null, MILLIS_IN_A_MINUTE, true);

            verify(schedulerService).safeScheduleRepeatingJob(job);
        } finally {
            stopFakingTime();
        }
    }
}
