package org.springframework.samples.travel.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.travel.domain.Booking;
import org.springframework.samples.travel.domain.Hotel;
import org.springframework.samples.travel.services.BookingService;
import org.springframework.samples.travel.services.SearchCriteria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.List;

/**
 * this is a standard Spring MVC controller that handles the web-page based interaction.
 * <p/>
 * This is the backend processing for the various web pages.
 * <p/>
 * For the RESTful service endpoints, see {@link org.springframework.samples.travel.rest.HotelsRestController}, which provide a generic,
 * reusable RESTful interface that can be consumed from all manner of clients. In a sense, both the {@link org.springframework.samples.travel.rest.HotelsRestController} and
 * the {@link HotelsMvcController} are "clients" to the {@link BookingService}.
 */
@Controller
public class HotelsMvcController {

    @Autowired
    private BookingService bookingService;

    @RequestMapping(value = "/hotels/search", method = RequestMethod.GET)
    public void search(SearchCriteria searchCriteria, Principal currentUser, Model model) {
        if (currentUser != null) {
            List<Booking> booking = bookingService.findBookings(currentUser.getName());
            model.addAttribute(booking);
        }
    }

    @RequestMapping(value = "/hotels", method = RequestMethod.GET)
    public String list(SearchCriteria criteria, Model model) {
        List<Hotel> hotels = bookingService.findHotels(criteria);
        model.addAttribute(hotels);
        return "hotels/list";
    }

    @RequestMapping(value = "/hotels/{id}", method = RequestMethod.GET)
    public String show(@PathVariable Long id, Model model) {
        model.addAttribute(bookingService.findHotelById(id));
        return "hotels/show";
    }

    @RequestMapping(value = "/bookings/{id}", method = RequestMethod.DELETE)
    public String deleteBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return "redirect:../hotels/search";
    }

}
