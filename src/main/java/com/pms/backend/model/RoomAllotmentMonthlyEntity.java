package com.pms.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "room_allotments_monthly")
public class RoomAllotmentMonthlyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String propertyId;

    @Column(nullable = false)
    private String roomTypeId;

    @Column(nullable = false)
    private Integer inYear;

    @Column(nullable = false)
    private Integer inMonth;

    private Integer col01; private Integer col02; private Integer col03; private Integer col04; private Integer col05;
    private Integer col06; private Integer col07; private Integer col08; private Integer col09; private Integer col10;
    private Integer col11; private Integer col12; private Integer col13; private Integer col14; private Integer col15;
    private Integer col16; private Integer col17; private Integer col18; private Integer col19; private Integer col20;
    private Integer col21; private Integer col22; private Integer col23; private Integer col24; private Integer col25;
    private Integer col26; private Integer col27; private Integer col28; private Integer col29; private Integer col30;
    private Integer col31;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }
    public Integer getInYear() { return inYear; }
    public void setInYear(Integer inYear) { this.inYear = inYear; }
    public Integer getInMonth() { return inMonth; }
    public void setInMonth(Integer inMonth) { this.inMonth = inMonth; }

    public Integer getCol(int day) {
        switch (day) {
            case 1: return col01; case 2: return col02; case 3: return col03; case 4: return col04; case 5: return col05;
            case 6: return col06; case 7: return col07; case 8: return col08; case 9: return col09; case 10: return col10;
            case 11: return col11; case 12: return col12; case 13: return col13; case 14: return col14; case 15: return col15;
            case 16: return col16; case 17: return col17; case 18: return col18; case 19: return col19; case 20: return col20;
            case 21: return col21; case 22: return col22; case 23: return col23; case 24: return col24; case 25: return col25;
            case 26: return col26; case 27: return col27; case 28: return col28; case 29: return col29; case 30: return col30;
            case 31: return col31;
            default: return null;
        }
    }

    public void setCol(int day, Integer value) {
        switch (day) {
            case 1: col01 = value; break; case 2: col02 = value; break; case 3: col03 = value; break; case 4: col04 = value; break; case 5: col05 = value; break;
            case 6: col06 = value; break; case 7: col07 = value; break; case 8: col08 = value; break; case 9: col09 = value; break; case 10: col10 = value; break;
            case 11: col11 = value; break; case 12: col12 = value; break; case 13: col13 = value; break; case 14: col14 = value; break; case 15: col15 = value; break;
            case 16: col16 = value; break; case 17: col17 = value; break; case 18: col18 = value; break; case 19: col19 = value; break; case 20: col20 = value; break;
            case 21: col21 = value; break; case 22: col22 = value; break; case 23: col23 = value; break; case 24: col24 = value; break; case 25: col25 = value; break;
            case 26: col26 = value; break; case 27: col27 = value; break; case 28: col28 = value; break; case 29: col29 = value; break; case 30: col30 = value; break;
            case 31: col31 = value; break;
        }
    }
}
